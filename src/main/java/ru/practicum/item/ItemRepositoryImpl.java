package ru.practicum.item;

import org.springframework.stereotype.Repository;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Item> findByUserId(long userId) {
        return items.values().stream()
                .filter(item -> userId == item.getOwnerId())
                .toList();
    }

    @Override
    public Optional<Item> findById(long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> search(String text) {
        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> {
                    String name = item.getName() == null ? "" : item.getName().toLowerCase();
                    String description = item.getDescription() == null ? "" : item.getDescription().toLowerCase();
                    return name.contains(lowerText) || description.contains(lowerText);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public void deleteByUserIdAndItemId(long userId, long itemId) {
        Item item = items.get(itemId);

        if (item == null) {
            throw new NotFoundException("Item not found");
        }

        if (!item.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Only owner can edit item");
        }

        items.remove(itemId);
    }
}
