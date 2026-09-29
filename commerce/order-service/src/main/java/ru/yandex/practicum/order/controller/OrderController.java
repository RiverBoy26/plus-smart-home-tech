package ru.yandex.practicum.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.service.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Создание заказа");

        return orderService.create(request);
    }

    @GetMapping("/{id}")
    public OrderDto getOrderById(@PathVariable Long id) {
        log.info("Запрос заказа по id={}", id);

        return orderService.getById(id);
    }

    @GetMapping
    public List<OrderDto> getAllOrders() {
        log.info("Запрос вывода всех заказов");

        return orderService.getAll();
    }

    @GetMapping("/by-email")
    public List<OrderDto> getOrdersByEmail(@RequestParam String email) {
        log.info("Запрос вывода заказов клиента");
        return orderService.getByEmail(email);
    }
}