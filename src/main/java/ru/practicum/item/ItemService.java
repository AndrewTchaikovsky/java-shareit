package ru.practicum.item;

import java.util.List;

public interface ItemService {
    List<ItemOwnerDto> getItems(long userId);

    ItemDto addNewItem(long userId, ItemDto dto);

    ItemOwnerDto getItemById(long userId, long itemId);

    ItemDto updateItem(long userId, long itemId, ItemDto dto);

    List<ItemDto> searchItems(String text);

    void deleteItem(long userId, long itemId);

    CommentDto addComment(Long userId, Long itemId, String text);

}
