package ru.practicum.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.item.Item;
import ru.practicum.item.ItemMapper;
import ru.practicum.user.User;
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

    public static Booking toBooking(BookingRequestDto dto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        return booking;
    }

}
