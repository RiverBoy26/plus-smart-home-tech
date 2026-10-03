package ru.yandex.practicum.inventory.dto;

public record ReserveResponse(
        boolean success,
        Long productId,
        Integer reservedQuantity,
        Integer availableQuantity
) {
}