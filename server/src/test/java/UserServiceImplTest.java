import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItServer.class)
@Transactional
public class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    void userWorkflowShouldCreateReadUpdateListAndDeleteUser() {
        UserDto created = userService.createUser(new UserDto(null, "user-test@email.com", "User"));
        UserDto updated = userService.updateUser(created.getId(), new UserDto(null, "updated-test@email.com", "Updated"));

        assertNotNull(created.getId());
        assertEquals("Updated", updated.getName());
        assertEquals(created.getId(), userService.getUserById(created.getId()).getId());
        assertFalse(userService.getAllUsers().isEmpty());

        userService.deleteUser(created.getId());

        assertThrows(NotFoundException.class, () -> userService.getUserById(created.getId()));
    }
}
