package ru.yandex.practicum.order.exception;

public class ProductUnavailableException extends RuntimeException {

    public ProductUnavailableException(String message) {
        super(message);
    }
}