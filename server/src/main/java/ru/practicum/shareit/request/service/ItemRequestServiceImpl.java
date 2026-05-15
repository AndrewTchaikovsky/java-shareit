package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest request = ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemRequest saved = itemRequestRepository.save(request);

        return ItemRequestMapper.toDto(saved);
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {

        validateUser(userId);

        return itemRequestRepository
                .findAllByRequestorIdOrderByCreatedDesc(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(
            Long userId,
            Integer from,
            Integer size
    ) {

        validateUser(userId);

        Pageable pageable = PageRequest.of(
                from / size,
                size,
                Sort.by(Sort.Direction.DESC, "created")
        );

        return itemRequestRepository
                .findAllByRequestorIdNot(userId, pageable)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {

        validateUser(userId);

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        return toResponseDto(request);
    }

    private ItemRequestResponseDto toResponseDto(ItemRequest request) {
        List<Item> items = itemRepository.findByRequestId(request.getId());
        return ItemRequestMapper.toResponseDto(request, items);
    }

    private void validateUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
    }
}
