package ru.practicum.booking;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(Long userId, BookingRequestDto dto);

    BookingResponseDto approve(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto get(Long userId, Long bookingId);

    List<BookingResponseDto> getAll(Long userId, State state);

    List<BookingResponseDto> getOwnerBookings(Long userId, State state);
}
