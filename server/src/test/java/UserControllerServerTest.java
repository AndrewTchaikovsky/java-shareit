import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = ShareItServer.class)
public class UserControllerServerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsersShouldReturn200() throws Exception {
        UserDto dto = new UserDto(1L, "user@email.com", "User");
        when(userService.getAllUsers()).thenReturn(List.of(dto));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getUserByIdShouldReturn200() throws Exception {
        UserDto dto = new UserDto(1L, "user@email.com", "User");
        when(userService.getUserById(anyLong())).thenReturn(dto);

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createUserShouldReturn200() throws Exception {
        UserDto input = new UserDto(null, "user@email.com", "User");
        UserDto returned = new UserDto(1L, "user@email.com", "User");
        when(userService.createUser(any())).thenReturn(returned);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(input))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateUserShouldReturn200() throws Exception {
        UserDto input = new UserDto(null, "updated@email.com", "Updated");
        UserDto returned = new UserDto(1L, "updated@email.com", "Updated");
        when(userService.updateUser(anyLong(), any())).thenReturn(returned);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(input))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteUserShouldReturn200() throws Exception {
        doNothing().when(userService).deleteUser(anyLong());

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}