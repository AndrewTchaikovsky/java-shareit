package ru.practicum.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.UserRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<Item> getItems(long userId) {
        return itemRepository.findByUserId(userId);
    }

    @Override
    public Item addNewItem(long userId, Item item) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        item.setOwnerId(userId);
        return itemRepository.save(item);
    }

    @Override
    public Item getItemById(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
    }

    @Override
    public Item updateItem(long userId, long itemId, Item updatedItem) {
        Item existingItem = getItemById(itemId);

        if (!existingItem.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Only owner can edit item");
        }

        if (updatedItem.getName() != null) {
            existingItem.setName(updatedItem.getName());
        }

        if (updatedItem.getDescription() != null) {
            existingItem.setDescription(updatedItem.getDescription());
        }

        if (updatedItem.getAvailable() != null) {
            existingItem.setAvailable(updatedItem.getAvailable());
        }

        return itemRepository.save(existingItem);
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text);
    }

    @Override
    public void deleteItem(long userId, long itemId) {

        itemRepository.deleteByUserIdAndItemId(userId, itemId);
    }
}