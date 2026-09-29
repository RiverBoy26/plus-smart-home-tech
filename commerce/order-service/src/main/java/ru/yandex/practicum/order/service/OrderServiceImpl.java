package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.entity.OrderStatus;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public OrderDto create(CreateOrderRequest request) {
        Order order = new Order();

        order.setCustomerName(request.customerName());

        order.setCustomerEmail(request.customerEmail());

        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest requestItem : request.items()) {
            OrderItem item = new OrderItem();

            item.setProductId(requestItem.productId());
            item.setProductName(requestItem.productName());
            item.setQuantity(requestItem.quantity());
            item.setPrice(requestItem.price());
            order.addItem(item);
            BigDecimal itemTotal = requestItem.price().multiply(BigDecimal.valueOf(requestItem.quantity()));

            total = total.add(itemTotal);
        }

        order.setTotalPrice(total);

        log.debug("Рассчитана итоговая стоимость заказа: {}", total);

        Order saved = orderRepository.save(order);

        log.debug("Заказ сохранён: id={}, status={}", saved.getId(), saved.getStatus());

        return toDto(saved);
    }

    @Override
    public OrderDto getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Заказ с id=" + id + " не найден"));

        log.debug("Заказ найден: id={}, позиций={}", order.getId(), order.getItems().size());

        return toDto(order);
    }

    @Override
    public List<OrderDto> getAll() {
        List<OrderDto> orders = orderRepository.findAll()
                        .stream()
                        .map(this::toDto)
                        .toList();

        log.debug("Получено заказов: {}", orders.size());

        return orders;
    }

    @Override
    public List<OrderDto> getByEmail(String email) {
        List<OrderDto> orders = orderRepository.findAllByCustomerEmailIgnoreCase(email)
                        .stream()
                        .map(this::toDto)
                        .toList();

        log.debug("По email найдено заказов: {}", orders.size());

        return orders;
    }

    private OrderDto toDto(Order order) {
        List<OrderItemDto> items = order.getItems()
                        .stream()
                        .map(item ->
                                new OrderItemDto(
                                        item.getId(),
                                        item.getProductId(),
                                        item.getProductName(),
                                        item.getQuantity(),
                                        item.getPrice()
                                )
                        )
                        .toList();

        return new OrderDto(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getStatusDetails(),
                order.getCreatedAt(),
                items
        );
    }
}