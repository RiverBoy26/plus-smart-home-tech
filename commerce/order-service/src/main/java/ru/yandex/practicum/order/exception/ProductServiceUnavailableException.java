package ru.yandex.practicum.order.exception;

public class ProductServiceUnavailableException extends RuntimeException {

    private final Long productId;

    public ProductServiceUnavailableException(Long productId, Throwable cause) {
        super("product-service технически недоступен для товара с id=" + productId, cause);

        this.productId = productId;
    }

    public Long productId() {
        return productId;
    }
}