package ru.practicum.item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    List<Item> findByUserId(long userId);

    Optional<Item> findById(long itemId);

    List<Item> search(String text);

    Item save(Item item);

    void deleteByUserIdAndItemId(long userId, long itemId);
}