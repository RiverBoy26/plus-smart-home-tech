package ru.yandex.practicum.order.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

@UtilityClass
public class OrderMapper {

    public Order toEntity(CreateOrderRequest request) {
        Order order = new Order();

        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setStatus(OrderStatus.CONFIRMED);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest requestItem : request.items()) {
            OrderItem item = new OrderItem();

            item.setProductId(requestItem.productId());
            item.setProductName(requestItem.productName());
            item.setQuantity(requestItem.quantity());
            item.setPrice(requestItem.price());

            order.addItem(item);

            BigDecimal itemTotal = requestItem.price()
                    .multiply(BigDecimal.valueOf(requestItem.quantity()));

            total = total.add(itemTotal);
        }

        order.setTotalPrice(total);

        return order;
    }

    public OrderDto toDto(Order order) {
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