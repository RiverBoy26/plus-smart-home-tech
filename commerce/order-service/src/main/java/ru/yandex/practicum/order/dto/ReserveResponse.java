package ru.yandex.practicum.order.dto;

public record ReserveResponse(
        boolean success,
        Long productId,
        Integer reservedQuantity,
        Integer availableQuantity,
        String message
) {
}