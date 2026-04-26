package ru.practicum.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.item.ItemMapper;
import ru.practicum.user.UserMapper;

@UtilityClass
public class BookingMapper {

    public static BookingResponseDto toDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                ItemMapper.toItemDto(booking.getItem()),
                UserMapper.toUserDto(booking.getBooker()),
                booking.getStatus()
        );
    }

    public static BookingShortDto toShort(Booking booking) {
        if (booking == null) return null;

        return new BookingShortDto(
                booking.getId(),
                booking.getBooker().getId()
        );
    }

}
