import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItServer.class)
@Transactional
public class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User savedUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        return userRepository.save(u);
    }

    private Item savedItem(String name, User owner, boolean available) {
        Item i = new Item();
        i.setName(name);
        i.setDescription("desc");
        i.setAvailable(available);
        i.setOwner(owner);
        return itemRepository.save(i);
    }

    @Test
    void createBookingShouldCreateBooking() {

        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@email.com");

        owner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@email.com");

        booker = userRepository.save(booker);

        Item item = new Item();
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);

        item = itemRepository.save(item);

        BookingRequestDto dto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        BookingResponseDto result = bookingService.create(booker.getId(), dto);

        assertNotNull(result.getId());

        assertEquals(Status.WAITING, result.getStatus());

    }

    @Test
    void createBookingForUnavailableItemShouldThrow() {

        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@email.com");

        owner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@email.com");

        User savedBooker = userRepository.save(booker);

        Item item = new Item();
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(false);
        item.setOwner(owner);

        item = itemRepository.save(item);

        BookingRequestDto dto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.create(savedBooker.getId(), dto)
        );

        assertEquals("Item is not available", exception.getMessage());

    }

    @Test
    void bookingWorkflowShouldApproveGetAndListBookings() {
        User owner = new User();
        owner.setName("Owner2");
        owner.setEmail("owner2@email.com");
        User savedOwner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Booker2");
        booker.setEmail("booker2@email.com");
        User savedBooker = userRepository.save(booker);

        Item item = new Item();
        item.setName("Item2");
        item.setDescription("Description2");
        item.setAvailable(true);
        item.setOwner(savedOwner);
        Item savedItem = itemRepository.save(item);

        BookingRequestDto dto = new BookingRequestDto(
                savedItem.getId(),
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4)
        );

        BookingResponseDto created = bookingService.create(savedBooker.getId(), dto);
        BookingResponseDto approved = bookingService.approve(savedOwner.getId(), created.getId(), true);
        BookingResponseDto found = bookingService.get(savedBooker.getId(), created.getId());

        assertEquals(Status.APPROVED, approved.getStatus());
        assertEquals(created.getId(), found.getId());
        assertEquals(1, bookingService.getAll(savedBooker.getId(), State.ALL, 0, 10).size());
        assertEquals(1, bookingService.getOwnerBookings(savedOwner.getId(), State.ALL, 0, 10).size());
    }

    @Test
    void createBookingWithNullStartShouldThrow() {
        User booker = savedUser("B", "b@e.com");
        User owner = savedUser("O", "o@e.com");
        Item item = savedItem("Item", owner, true);

        BookingRequestDto dto = new BookingRequestDto(item.getId(), null, LocalDateTime.now().plusDays(1));

        assertThrows(IllegalArgumentException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void createBookingWithStartAfterEndShouldThrow() {
        User booker = savedUser("B2", "b2@e.com");
        User owner = savedUser("O2", "o2@e.com");
        Item item = savedItem("Item2", owner, true);

        BookingRequestDto dto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(IllegalArgumentException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void createBookingWithEqualStartAndEndShouldThrow() {
        User booker = savedUser("B3", "b3@e.com");
        User owner = savedUser("O3", "o3@e.com");
        Item item = savedItem("Item3", owner, true);

        LocalDateTime time = LocalDateTime.now().plusDays(1);
        BookingRequestDto dto = new BookingRequestDto(item.getId(), time, time);

        assertThrows(IllegalArgumentException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void createBookingForNonExistentUserShouldThrow() {
        User owner = savedUser("O4", "o4@e.com");
        Item item = savedItem("Item4", owner, true);

        BookingRequestDto dto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        assertThrows(NotFoundException.class, () -> bookingService.create(999L, dto));
    }

    @Test
    void createBookingForNonExistentItemShouldThrow() {
        User booker = savedUser("B5", "b5@e.com");

        BookingRequestDto dto = new BookingRequestDto(
                999L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        assertThrows(NotFoundException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void ownerBookingOwnItemShouldThrow() {
        User owner = savedUser("O5", "o5@e.com");
        Item item = savedItem("Item5", owner, true);

        BookingRequestDto dto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        assertThrows(NotFoundException.class, () -> bookingService.create(owner.getId(), dto));
    }

    @Test
    void createBookingOverlappingShouldThrow() {
        User owner = savedUser("O6", "o6@e.com");
        User booker1 = savedUser("B6a", "b6a@e.com");
        User booker2 = savedUser("B6b", "b6b@e.com");
        Item item = savedItem("Item6", owner, true);

        BookingRequestDto dto1 = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)
        );
        BookingResponseDto created = bookingService.create(booker1.getId(), dto1);

        bookingService.approve(owner.getId(), created.getId(), true);

        BookingRequestDto dto2 = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(4)
        );

        assertThrows(ConflictException.class, () -> bookingService.create(booker2.getId(), dto2));
    }

    @Test
    void approveByNonOwnerShouldThrow() {
        User owner = savedUser("O7", "o7@e.com");
        User booker = savedUser("B7", "b7@e.com");
        User other = savedUser("Other", "other@e.com");
        Item item = savedItem("Item7", owner, true);

        BookingRequestDto dto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        BookingResponseDto created = bookingService.create(booker.getId(), dto);

        assertThrows(AccessDeniedException.class,
                () -> bookingService.approve(other.getId(), created.getId(), true));
    }

    @Test
    void approvingAlreadyProcessedBookingShouldThrow() {
        User owner = savedUser("O8", "o8@e.com");
        User booker = savedUser("B8", "b8@e.com");
        Item item = savedItem("Item8", owner, true);

        BookingRequestDto dto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        BookingResponseDto created = bookingService.create(booker.getId(), dto);
        bookingService.approve(owner.getId(), created.getId(), true);

        assertThrows(ConflictException.class,
                () -> bookingService.approve(owner.getId(), created.getId(), false));
    }

    @Test
    void approveNonExistentBookingShouldThrow() {
        assertThrows(NotFoundException.class,
                () -> bookingService.approve(1L, 999L, true));
    }

    @Test
    void getBookingByUnrelatedUserShouldThrow() {
        User owner = savedUser("O9", "o9@e.com");
        User booker = savedUser("B9", "b9@e.com");
        User other = savedUser("Other2", "other2@e.com");
        Item item = savedItem("Item9", owner, true);

        BookingRequestDto dto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        BookingResponseDto created = bookingService.create(booker.getId(), dto);

        assertThrows(AccessDeniedException.class,
                () -> bookingService.get(other.getId(), created.getId()));
    }

    @Test
    void getNonExistentBookingShouldThrow() {
        assertThrows(NotFoundException.class, () -> bookingService.get(1L, 999L));
    }

    @Test
    void getAllBookingsForAllStates() {
        User owner = savedUser("O10", "o10@e.com");
        User booker = savedUser("B10", "b10@e.com");
        Item item = savedItem("Item10", owner, true);

        BookingRequestDto dto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        bookingService.create(booker.getId(), dto);

        assertEquals(1, bookingService.getAll(booker.getId(), State.ALL, 0, 10).size());
        assertEquals(0, bookingService.getAll(booker.getId(), State.CURRENT, 0, 10).size());
        assertEquals(0, bookingService.getAll(booker.getId(), State.PAST, 0, 10).size());
        assertEquals(1, bookingService.getAll(booker.getId(), State.FUTURE, 0, 10).size());
        assertEquals(1, bookingService.getAll(booker.getId(), State.WAITING, 0, 10).size());
        assertEquals(0, bookingService.getAll(booker.getId(), State.REJECTED, 0, 10).size());
    }

    @Test
    void getOwnerBookingsForAllStates() {
        User owner = savedUser("O11", "o11@e.com");
        User booker = savedUser("B11", "b11@e.com");
        Item item = savedItem("Item11", owner, true);

        BookingRequestDto dto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        bookingService.create(booker.getId(), dto);

        assertEquals(1, bookingService.getOwnerBookings(owner.getId(), State.ALL, 0, 10).size());
        assertEquals(0, bookingService.getOwnerBookings(owner.getId(), State.CURRENT, 0, 10).size());
        assertEquals(0, bookingService.getOwnerBookings(owner.getId(), State.PAST, 0, 10).size());
        assertEquals(1, bookingService.getOwnerBookings(owner.getId(), State.FUTURE, 0, 10).size());
        assertEquals(1, bookingService.getOwnerBookings(owner.getId(), State.WAITING, 0, 10).size());
        assertEquals(0, bookingService.getOwnerBookings(owner.getId(), State.REJECTED, 0, 10).size());
    }

}
