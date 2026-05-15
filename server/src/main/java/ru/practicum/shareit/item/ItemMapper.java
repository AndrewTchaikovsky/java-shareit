package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;

@UtilityClass
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {

        if (item == null) {
            return null;
        }

        Long requestId = item.getRequest() != null
                ? item.getRequest().getId()
                : null;

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                requestId,
                null
        );
    }

    public static Item toItem(ItemDto dto) {

        if (dto == null) {
            return null;
        }

        Item item = new Item();

        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());

        return item;
    }
}
