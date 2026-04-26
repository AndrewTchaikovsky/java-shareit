package ru.practicum.booking;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.item.Item;
import ru.practicum.item.ItemRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingResponseDto create(Long userId, BookingRequestDto dto) {

        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }

        if (dto.getStart().isAfter(dto.getEnd()) || dto.getStart().isEqual(dto.getEnd())) {
            throw new IllegalArgumentException("Start must be before end");
        }

        if (dto.getStart().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start must not be in the past");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Owner cannot book own item");
        }

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Item is not available");
        }

        if (bookingRepository.existOverlapping(
                item.getId(),
                dto.getStart(),
                dto.getEnd()
        )) {
            throw new ConflictException("Item is already booked for this period");
        }

        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findByIdWithRelations(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only owner can approve booking");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ConflictException("Booking is already processed");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);

        return BookingMapper.toDto(bookingRepository.save(booking));

    }

    @Override
    public BookingResponseDto get(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findByIdWithRelations(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only owner can get booking");
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAll(Long userId, State state) {
        return getBookingByStateForBooker(userId, state);
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, State state) {
        return getBookingsByStateForOwner(userId, state);
    }

    private List<BookingResponseDto> getBookingByStateForBooker(Long userId, State state) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBooker_Id(userId, sort);
            case CURRENT -> bookingRepository.findByBooker_IdAndStartBeforeAndEndAfter(userId, now, now, sort);
            case PAST -> bookingRepository.findByBooker_IdAndEndBefore(userId, now, sort);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartAfter(userId, now, sort);
            case WAITING -> bookingRepository.findByBooker_IdAndStatus(userId, Status.WAITING, sort);
            case REJECTED -> bookingRepository.findByBooker_IdAndStatus(userId, Status.REJECTED, sort);
        };
        return bookings.stream().map(BookingMapper::toDto).toList();
    }

    private List<BookingResponseDto> getBookingsByStateForOwner(Long userId, State state) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItem_Owner_Id(userId, sort);
            case CURRENT -> bookingRepository.findByItem_Owner_IdAndStartBeforeAndEndAfter(userId, now, now, sort);
            case PAST -> bookingRepository.findByItem_Owner_IdAndEndBefore(userId, now, sort);
            case FUTURE -> bookingRepository.findByItem_Owner_IdAndStartAfter(userId, now, sort);
            case WAITING -> bookingRepository.findByItem_Owner_IdAndStatus(userId, Status.WAITING, sort);
            case REJECTED -> bookingRepository.findByItem_Owner_IdAndStatus(userId, Status.REJECTED, sort);
        };
        return bookings.stream().map(BookingMapper::toDto).toList();
    }


}
