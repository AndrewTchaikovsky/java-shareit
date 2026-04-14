package ru.practicum.item;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final List<Item> items = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Item> findByUserId(long userId) {
        return items.stream()
                .filter(item -> item.getUserId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
        }
        items.add(item);
        return item;
    }

    @Override
    public void deleteByUserIdAndItemId(long userId, long itemId) {
        items.removeIf(item -> item.getUserId() == userId && itemId == item.getId());
    }
}
