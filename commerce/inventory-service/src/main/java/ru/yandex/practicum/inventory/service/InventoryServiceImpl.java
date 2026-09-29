package ru.yandex.practicum.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.Inventory;
import ru.yandex.practicum.inventory.exception.InsufficientStockException;
import ru.yandex.practicum.inventory.exception.NotFoundException;
import ru.yandex.practicum.inventory.repository.InventoryRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    public List<InventoryDto> getAll() {

        List<InventoryDto> inventory = inventoryRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();

        log.debug("Получено складских записей: {}", inventory.size());

        return inventory;
    }

    @Override
    public InventoryDto getByProductId(Long productId) {
        Inventory inventory = findByProductId(productId);

        log.debug(
                "Складская запись найдена: quantity={}, reserved={}, available={}",
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity()
        );

        return toDto(inventory);
    }

    @Override
    @Transactional
    public InventoryDto create(UpdateInventoryRequest request) {
        if (inventoryRepository.existsByProductId(request.productId())) {
            throw new IllegalArgumentException("Складская запись для товара с productId=" +
                    request.productId() + " уже существует");
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(request.productId());
        inventory.setQuantity(request.quantity());
        inventory.setReservedQuantity(0);

        Inventory saved = inventoryRepository.save(inventory);

        log.debug(
                "Складская запись создана: id={}, productId={}",
                saved.getId(),
                saved.getProductId()
        );

        return toDto(saved);
    }

    @Override
    @Transactional
    public InventoryDto update(UpdateInventoryRequest request) {
        Inventory inventory = findByProductId(request.productId());

        if (request.quantity() < inventory.getReservedQuantity()) {
            log.debug("Невозможно обновить productId={}: reservedQuantity={}",
                    request.productId(),
                    inventory.getReservedQuantity()
            );

            throw new InsufficientStockException("Общее количество товара не может быть меньше зарезервированного количества: " +
                    inventory.getReservedQuantity());
        }

        inventory.setQuantity(request.quantity());

        Inventory saved = inventoryRepository.saveAndFlush(inventory);

        log.debug(
                "Количество обновлено: productId={}, available={}",
                saved.getProductId(),
                saved.getAvailableQuantity()
        );

        return toDto(saved);
    }

    @Override
    @Transactional
    public ReserveResponse reserve(ReserveRequest request) {
        Inventory inventory = findByProductId(request.productId());

        int availableQuantity = inventory.getAvailableQuantity();

        if (request.quantity() > availableQuantity) {
            throw new InsufficientStockException("Недостаточно товара с productId=" + request.productId() +
                    ". Доступно: " + availableQuantity + ", запрошено: " + request.quantity());
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.quantity());

        Inventory saved = inventoryRepository.saveAndFlush(inventory);

        log.debug(
                "Товар зарезервирован: productId={}, available={}",
                saved.getProductId(),
                saved.getAvailableQuantity()
        );

        return new ReserveResponse(true, saved.getAvailableQuantity(), "Товар успешно зарезервирован");
    }

    private Inventory findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Складская запись для товара с productId=" +
                                        productId + " не найдена"));
    }

    private InventoryDto toDto(Inventory inventory) {
        return new InventoryDto(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity()
        );
    }
}