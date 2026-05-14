import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.service.UserService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = ShareItServer.class)
public class ExceptionHandlerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @Test
    void notFoundExceptionShouldReturn404() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void forbiddenExceptionShouldReturn403() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new AccessDeniedException("Access denied"));

        mvc.perform(get("/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void conflictExceptionShouldReturn409() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new ConflictException("Conflict"));

        mvc.perform(get("/users/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void illegalArgumentExceptionShouldReturn400() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new IllegalArgumentException("Bad argument"));

        mvc.perform(get("/users/1"))
                .andExpect(status().isBadRequest());
    }
}