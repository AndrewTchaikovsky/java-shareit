import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItServer.class)
@Transactional
public class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void requestWorkflowShouldCreateReadOwnAndAllRequests() {
        User requestor = new User();
        requestor.setName("Requestor");
        requestor.setEmail("requestor@email.com");
        User savedRequestor = userRepository.save(requestor);

        User viewer = new User();
        viewer.setName("Viewer");
        viewer.setEmail("viewer@email.com");
        User savedViewer = userRepository.save(viewer);

        ItemRequestDto created = itemRequestService.create(
                savedRequestor.getId(),
                ItemRequestDto.builder().description("Need item").build()
        );

        itemService.addNewItem(
                savedViewer.getId(),
                new ItemDto(null, "Answer item", "Description", true, created.getId(), null)
        );

        ItemRequestResponseDto found = itemRequestService.getRequestById(savedRequestor.getId(), created.getId());

        assertNotNull(created.getId());
        assertEquals("Need item", found.getDescription());
        assertEquals(1, found.getItems().size());
        assertEquals(savedViewer.getId(), found.getItems().getFirst().getOwnerId());
        assertEquals(1, itemRequestService.getOwnRequests(savedRequestor.getId()).size());
        assertEquals(1, itemRequestService.getAllRequests(savedViewer.getId(), 0, 10).size());
    }
}
