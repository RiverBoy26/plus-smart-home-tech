package ru.yandex.practicum.order.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.dto.ProductDto;
import ru.yandex.practicum.order.dto.ReserveRequest;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderStatus;
import ru.yandex.practicum.order.exception.InsufficientStockException;
import ru.yandex.practicum.order.exception.InventoryServiceUnavailableException;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.exception.ProductServiceUnavailableException;
import ru.yandex.practicum.order.exception.ProductUnavailableException;
import ru.yandex.practicum.order.feign.InventoryClient;
import ru.yandex.practicum.order.feign.ProductClient;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    @Override
    public OrderDto create(CreateOrderRequest request) {
        Map<Long, Integer> quantities = request.items()
                .stream()
                .collect(Collectors.groupingBy(
                        OrderItemRequest::productId,
                        Collectors.summingInt(OrderItemRequest::quantity)
                ));

        Map<Long, ProductDto> products = new HashMap<>();
        List<ReserveRequest> reservations = new ArrayList<>();
        List<String> degradationReasons = new ArrayList<>();

        boolean inventoryDegraded = false;

        try {
            for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
                Long productId = entry.getKey();
                Integer quantity = entry.getValue();

                ProductDto product;

                try {
                    product = productClient.getProductById(productId);

                } catch (FeignException.NotFound e) {
                    throw new NotFoundException(
                            "Товар с id=" + productId + " не найден"
                    );

                } catch (ProductServiceUnavailableException e) {
                    log.warn("product-service недоступен, заказ будет сохранён в статусе PENDING_CONFIRMATION: "
                                    + "productId={}", productId);

                    product = new ProductDto(productId, "Товар №" + productId + " (ожидает проверки)",
                            null, BigDecimal.ZERO, null);

                    degradationReasons.add("Не удалось получить данные товара с id=" + productId);

                } catch (FeignException e) {
                    log.warn("Техническая ошибка product-service, заказ будет сохранён для ручной проверки: "
                                    + "productId={}, status={}", productId, e.status());

                    product = new ProductDto(productId, "Товар №" + productId + " (ожидает проверки)", null,
                            BigDecimal.ZERO, null);

                    degradationReasons.add("Не удалось получить данные товара с id=" + productId);
                }

                if (product.active() != null && !product.active()) {
                    throw new ProductUnavailableException(
                            "Товар с id=" + productId + " снят с продажи"
                    );
                }

                products.put(productId, product);

                ReserveRequest reserveRequest = new ReserveRequest(productId, quantity);

                try {
                    inventoryClient.reserveStock(reserveRequest);

                    reservations.add(reserveRequest);

                } catch (FeignException.NotFound e) {
                    throw new NotFoundException("Складская запись для товара с productId="
                                    + productId + " не найдена");

                } catch (FeignException.Conflict e) {
                    throw new InsufficientStockException("Недостаточно товара с productId=" + productId + " в количестве " + quantity);

                } catch (InventoryServiceUnavailableException e) {
                    log.warn("inventory-service недоступен, резервирование не подтверждено: "
                                    + "productId={}", productId);

                    inventoryDegraded = true;

                    degradationReasons.add("Резервирование товара с id="
                                    + productId + " не подтверждено"
                    );

                } catch (FeignException e) {
                    log.warn(
                            "Техническая ошибка inventory-service, резервирование не подтверждено: "
                                    + "productId={}, status={}", productId, e.status());

                    inventoryDegraded = true;

                    degradationReasons.add("Резервирование товара с id="
                                    + productId + " не подтверждено");
                }
            }

            if (inventoryDegraded && !reservations.isEmpty()) {
                try {
                    inventoryClient.releaseStock(reservations);
                    reservations.clear();

                } catch (RuntimeException releaseException) {
                    log.error(
                            "Не удалось снять резервы после перехода заказа "
                                    + "в деградированный режим: reservations={}",
                            reservations,
                            releaseException
                    );
                }
            }

            List<OrderItemRequest> items = request.items()
                    .stream()
                    .map(item -> {
                        ProductDto product = products.get(
                                item.productId()
                        );

                        return new OrderItemRequest(
                                product.id(),
                                product.name(),
                                item.quantity(),
                                product.price()
                        );
                    })
                    .toList();

            CreateOrderRequest actualRequest = new CreateOrderRequest(request.customerName(), request.customerEmail(), items);

            boolean degraded = !degradationReasons.isEmpty();

            OrderStatus status = degraded
                    ? OrderStatus.PENDING_CONFIRMATION
                    : OrderStatus.CONFIRMED;

            String statusDetails = degraded
                    ? "Требуется ручная проверка. "
                    + String.join("; ", degradationReasons)
                    : null;

            Order order = OrderMapper.toEntity(
                    actualRequest,
                    status,
                    statusDetails
            );

            Order saved = orderRepository.save(order);

            log.debug(
                    "Заказ сохранён: id={}, status={}, totalPrice={}",
                    saved.getId(),
                    saved.getStatus(),
                    saved.getTotalPrice()
            );

            return OrderMapper.toDto(saved);

        } catch (RuntimeException e) {
            if (!reservations.isEmpty()) {
                try {
                    inventoryClient.releaseStock(reservations);

                } catch (RuntimeException releaseException) {
                    log.error(
                            "Не удалось компенсировать резервы: reservations={}",
                            reservations,
                            releaseException
                    );
                }
            }

            throw e;
        }
    }

    @Override
    public OrderDto getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Заказ с id="
                                        + id + " не найден"));

        log.debug(
                "Заказ найден: id={}, позиций={}",
                order.getId(),
                order.getItems().size()
        );

        return OrderMapper.toDto(order);
    }

    @Override
    public List<OrderDto> getAll() {
        List<OrderDto> orders = orderRepository.findAll()
                        .stream()
                        .map(OrderMapper::toDto)
                        .toList();

        log.debug("Получено заказов: {}", orders.size());

        return orders;
    }

    @Override
    public List<OrderDto> getByEmail(String email) {
        List<OrderDto> orders = orderRepository
                        .findAllByCustomerEmailIgnoreCase(email)
                        .stream()
                        .map(OrderMapper::toDto)
                        .toList();

        log.debug("По email найдено заказов: {}", orders.size());

        return orders;
    }
}