package ru.yandex.practicum.inventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.service.InventoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryDto> getAllInventory() {
        log.info("Запрос всех складских записей");
        return inventoryService.getAll();
    }

    @GetMapping("/{productId}")
    public InventoryDto getByProductId(@PathVariable Long productId) {
        log.info("Запрос остатков товара");

        return inventoryService.getByProductId(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryDto createInventory(@Valid @RequestBody UpdateInventoryRequest request) {
        log.info("Создание складской записи");

        return inventoryService.create(request);
    }

    @PutMapping
    public InventoryDto updateInventory(@Valid @RequestBody UpdateInventoryRequest request) {
        log.info("Обновление остатков productId={}", request.productId());

        return inventoryService.update(request);
    }

    @PostMapping("/reserve")
    public ReserveResponse reserveStock(@Valid @RequestBody ReserveRequest request) {
        log.info("Резервирование productId={}", request.productId());

        return inventoryService.reserve(request);
    }

    @PostMapping("/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@RequestBody ReserveRequest request) {
        inventoryService.release(request);
    }
}