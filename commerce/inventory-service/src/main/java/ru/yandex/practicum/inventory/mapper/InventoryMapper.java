package ru.yandex.practicum.inventory.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.Inventory;

@UtilityClass
public class InventoryMapper {

    public Inventory toEntity(UpdateInventoryRequest request) {
        Inventory inventory = new Inventory();

        inventory.setProductId(request.productId());
        inventory.setQuantity(request.quantity());
        inventory.setReservedQuantity(0);

        return inventory;
    }

    public void updateEntity(
            Inventory inventory,
            UpdateInventoryRequest request
    ) {
        inventory.setQuantity(request.quantity());
    }

    public InventoryDto toDto(Inventory inventory) {
        return new InventoryDto(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity()
        );
    }
}