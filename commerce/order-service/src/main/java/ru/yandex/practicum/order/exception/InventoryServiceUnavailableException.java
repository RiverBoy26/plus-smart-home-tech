package ru.yandex.practicum.order.exception;

public class InventoryServiceUnavailableException extends RuntimeException {

    private final Long productId;

    public InventoryServiceUnavailableException(Long productId, Throwable cause) {
        super("inventory-service технически недоступен для товара с id=" + productId, cause);

        this.productId = productId;
    }

    public Long productId() {
        return productId;
    }
}