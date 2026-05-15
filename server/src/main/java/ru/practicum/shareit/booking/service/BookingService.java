package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(Long userId, BookingRequestDto dto);

    BookingResponseDto approve(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto get(Long userId, Long bookingId);

    List<BookingResponseDto> getAll(Long userId, State state, Integer from, Integer size);

    List<BookingResponseDto> getOwnerBookings(Long userId, State state, Integer from, Integer size);
}
