import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.Status;
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
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

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
}
