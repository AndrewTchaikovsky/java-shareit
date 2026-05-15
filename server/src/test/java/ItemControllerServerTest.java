import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@ContextConfiguration(classes = ShareItServer.class)
public class ItemControllerServerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Test
    void getItemsShouldReturn200() throws Exception {
        ItemOwnerDto dto = new ItemOwnerDto();
        dto.setId(1L);
        dto.setName("Drill");
        dto.setDescription("Power drill");
        dto.setAvailable(true);
        dto.setComments(List.of());
        when(itemService.getItems(anyLong())).thenReturn(List.of(dto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void addItemShouldReturn200() throws Exception {
        ItemDto dto = new ItemDto(null, "Drill", "Power drill", true, null, null);
        ItemDto returned = new ItemDto(1L, "Drill", "Power drill", true, null, null);
        when(itemService.addNewItem(anyLong(), any())).thenReturn(returned);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateItemShouldReturn200() throws Exception {
        ItemDto dto = new ItemDto(null, "Updated", null, null, null, null);
        ItemDto returned = new ItemDto(1L, "Updated", "Power drill", true, null, null);
        when(itemService.updateItem(anyLong(), anyLong(), any())).thenReturn(returned);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void getItemByIdShouldReturn200() throws Exception {
        ItemOwnerDto dto = new ItemOwnerDto();
        dto.setId(1L);
        dto.setName("Drill");
        dto.setDescription("Power drill");
        dto.setAvailable(true);
        dto.setComments(List.of());
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(dto);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void searchItemsShouldReturn200() throws Exception {
        ItemDto dto = new ItemDto(1L, "Drill", "Power drill", true, null, null);
        when(itemService.searchItems(anyString())).thenReturn(List.of(dto));

        mvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void addCommentShouldReturn200() throws Exception {
        CommentDto comment = new CommentDto(1L, "Great item", "User", null);
        when(itemService.addComment(anyLong(), anyLong(), anyString())).thenReturn(comment);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(new CommentDto(null, "Great item", null, null)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great item"));
    }

    @Test
    void deleteItemShouldReturn200() throws Exception {
        doNothing().when(itemService).deleteItem(anyLong(), anyLong());

        mvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }
}