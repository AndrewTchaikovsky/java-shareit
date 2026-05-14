package ru.practicum.shareit.request;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public class ItemRequestMapper {

    public static ItemRequestDto toDto(ItemRequest request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .build();
    }

    public static ItemRequestResponseDto toResponseDto(ItemRequest request, List<Item> items) {
        return ItemRequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items.stream()
                        .map(ItemRequestMapper::toAnswerDto)
                        .toList())
                .build();
    }

    private static ItemRequestAnswerDto toAnswerDto(Item item) {
        Long ownerId = item.getOwner() != null ? item.getOwner().getId() : null;
        return new ItemRequestAnswerDto(item.getId(), item.getName(), ownerId);
    }
}
