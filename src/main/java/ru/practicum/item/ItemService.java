package ru.practicum.item;

import java.util.List;

public interface ItemService {
    List<Item> getItems(long userId);

    Item addNewItem(long userId, Item item);

    Item getItemById(long itemId);

    Item updateItem(long userId, long itemId, Item item);

    List<Item> searchItems(String text);

    void deleteItem(long userId, long itemId);

}
