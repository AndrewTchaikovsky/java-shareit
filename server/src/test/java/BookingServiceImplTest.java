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

}
