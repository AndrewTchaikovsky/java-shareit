package ru.practicum.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.booking.Booking;
import ru.practicum.booking.BookingMapper;
import ru.practicum.booking.BookingRepository;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<ItemOwnerDto> getItems(long userId) {
        return itemRepository.findByOwnerId(userId)
                .stream()
                .map(this::enrich)
                .toList();
    }

    @Override
    public ItemDto addNewItem(long userId, ItemDto dto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item item = ItemMapper.toItem(dto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemOwnerDto getItemById(long userId, long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        ItemOwnerDto dto = enrich(item);

        List<CommentDto> comments = commentRepository
                .findByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(CommentMapper::toDto)
                .toList();

        dto.setComments(comments);

        boolean isOwner = item.getOwner().getId().equals(userId);

        if (!isOwner) {
            dto.setLastBooking(null);
            dto.setNextBooking(null);
        }

        return dto;
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto dto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only owner can edit item");
        }

        if (dto.getName() != null) {
            existingItem.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            existingItem.setDescription(dto.getDescription());
        }

        if (dto.getAvailable() != null) {
            existingItem.setAvailable(dto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.save(existingItem));
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public void deleteItem(long userId, long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only owner can delete item");
        }

        itemRepository.delete(item);
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, String text) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        boolean hasBooking = bookingRepository
                .existsByItemIdAndBookerIdAndEndBefore(itemId, userId, LocalDateTime.now());

        if (!hasBooking) {
            throw new IllegalArgumentException("User has not booked this item");
        }

        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toDto(commentRepository.save(comment));
    }

    private ItemOwnerDto enrich(Item item) {

        LocalDateTime now = LocalDateTime.now();

        Booking last = bookingRepository
                .findTopByItemIdAndStartBeforeOrderByStartDesc(item.getId(), now)
                .orElse(null);

        Booking next = bookingRepository
                .findTopByItemIdAndStartAfterOrderByStartAsc(item.getId(), now)
                .orElse(null);

        List<CommentDto> comments = commentRepository
                .findByItemIdOrderByCreatedDesc(item.getId())
                .stream()
                .map(CommentMapper::toDto)
                .toList();

        ItemOwnerDto dto = new ItemOwnerDto();

        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setRequestId(item.getRequestId());

        dto.setLastBooking(BookingMapper.toShort(last));
        dto.setNextBooking(BookingMapper.toShort(next));
        dto.setComments(comments);

        return dto;
    }
}