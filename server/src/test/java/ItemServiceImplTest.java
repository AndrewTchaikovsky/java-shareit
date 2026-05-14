import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItServer.class)
@Transactional
public class ItemServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private BookingRepository bookingRepository;

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
    void itemWorkflowShouldCreateReadUpdateSearchCommentAndDeleteItem() {
        User owner = new User();
        owner.setName("ItemOwner");
        owner.setEmail("item-owner@email.com");
        User savedOwner = userRepository.save(owner);

        User booker = new User();
        booker.setName("ItemBooker");
        booker.setEmail("item-booker@email.com");
        User savedBooker = userRepository.save(booker);

        ItemDto created = itemService.addNewItem(
                savedOwner.getId(),
                new ItemDto(null, "Drill", "Power drill", true, null, null)
        );
        ItemDto updated = itemService.updateItem(
                savedOwner.getId(),
                created.getId(),
                new ItemDto(null, "Updated drill", null, null, null, null)
        );
        ItemOwnerDto found = itemService.getItemById(savedOwner.getId(), created.getId());

        Item item = itemRepository.findById(created.getId()).orElseThrow();
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(savedBooker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(Status.APPROVED);
        bookingRepository.save(booking);

        CommentDto comment = itemService.addComment(savedBooker.getId(), created.getId(), "Good item");

        ItemDto itemToDelete = itemService.addNewItem(
                savedOwner.getId(),
                new ItemDto(null, "Saw", "Hand saw", true, null, null)
        );
        itemService.deleteItem(savedOwner.getId(), itemToDelete.getId());

        assertEquals("Updated drill", updated.getName());
        assertEquals(created.getId(), found.getId());
        assertEquals(1, itemService.getItems(savedOwner.getId()).size());
        assertEquals(1, itemService.searchItems("drill").size());
        assertEquals("Good item", comment.getText());
        assertTrue(itemRepository.findById(itemToDelete.getId()).isEmpty());
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

    @Test
    void addItemForNonExistentUserShouldThrow() {
        ItemDto dto = new ItemDto(null, "Drill", "desc", true, null, null);
        assertThrows(NotFoundException.class, () -> itemService.addNewItem(999L, dto));
    }

    @Test
    void addItemWithNonExistentRequestShouldThrow() {
        User owner = savedUser("Owner1", "owner1@e.com");
        ItemDto dto = new ItemDto(null, "Drill", "desc", true, 999L, null);
        assertThrows(NotFoundException.class, () -> itemService.addNewItem(owner.getId(), dto));
    }

    @Test
    void getItemByIdForNonOwnerShouldHideBookings() {
        User owner = savedUser("Owner2", "owner2@e.com");
        User other = savedUser("Other", "other@e.com");
        ItemDto created = itemService.addNewItem(owner.getId(),
                new ItemDto(null, "Drill", "desc", true, null, null));
        ItemOwnerDto dto = itemService.getItemById(other.getId(), created.getId());
        assertNull(dto.getLastBooking());
        assertNull(dto.getNextBooking());
    }

    @Test
    void getItemByIdForNonExistentItemShouldThrow() {
        assertThrows(NotFoundException.class, () -> itemService.getItemById(1L, 999L));
    }

    @Test
    void updateItemByNonOwnerShouldThrow() {
        User owner = savedUser("Owner4", "owner4@e.com");
        User other = savedUser("Other2", "other2@e.com");
        ItemDto created = itemService.addNewItem(owner.getId(),
                new ItemDto(null, "Drill", "desc", true, null, null));
        assertThrows(AccessDeniedException.class,
                () -> itemService.updateItem(other.getId(), created.getId(),
                        new ItemDto(null, "Hacked", null, null, null, null)));
    }

    @Test
    void updateItemForNonExistentItemShouldThrow() {
        User owner = savedUser("Owner5", "owner5@e.com");
        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(owner.getId(), 999L,
                        new ItemDto(null, "X", null, null, null, null)));
    }

    @Test
    void updateItemShouldUpdateOnlyProvidedFields() {
        User owner = savedUser("Owner6", "owner6@e.com");
        ItemDto created = itemService.addNewItem(owner.getId(),
                new ItemDto(null, "Drill", "Power drill", true, null, null));
        ItemDto updated = itemService.updateItem(owner.getId(), created.getId(),
                new ItemDto(null, null, null, false, null, null));
        assertEquals("Drill", updated.getName());
        assertEquals("Power drill", updated.getDescription());
        assertFalse(updated.getAvailable());
    }

    @Test
    void deleteItemByNonOwnerShouldThrow() {
        User owner = savedUser("Owner7", "owner7@e.com");
        User other = savedUser("Other3", "other3@e.com");
        ItemDto created = itemService.addNewItem(owner.getId(),
                new ItemDto(null, "Drill", "desc", true, null, null));
        assertThrows(AccessDeniedException.class,
                () -> itemService.deleteItem(other.getId(), created.getId()));
    }

    @Test
    void deleteNonExistentItemShouldThrow() {
        assertThrows(NotFoundException.class, () -> itemService.deleteItem(1L, 999L));
    }

    @Test
    void searchWithBlankTextShouldReturnEmpty() {
        assertTrue(itemService.searchItems("   ").isEmpty());
    }

    @Test
    void searchWithNullTextShouldReturnEmpty() {
        assertTrue(itemService.searchItems(null).isEmpty());
    }

    @Test
    void addCommentForNonExistentUserShouldThrow() {
    }

}