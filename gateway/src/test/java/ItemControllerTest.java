import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureMockMvc
public class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void getItemsShouldReturn200() throws Exception {
        when(itemClient.getItems(anyLong())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void addItemShouldReturn200() throws Exception {
        ItemDto dto = new ItemDto(null, "Item", "Description", true, null, null);
        when(itemClient.addItem(anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateItemShouldReturn200() throws Exception {
        ItemDto dto = new ItemDto(null, "Updated", null, null, null, null);
        when(itemClient.updateItem(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getItemShouldReturn200() throws Exception {
        when(itemClient.getItem(anyLong(), anyLong())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void searchItemsShouldReturn200() throws Exception {
        when(itemClient.searchItems(anyString())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items/search")
                        .param("text", "item"))
                .andExpect(status().isOk());
    }

    @Test
    void addCommentShouldReturn200() throws Exception {
        CommentDto dto = new CommentDto(null, "Text", null, null);
        when(itemClient.addComment(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItemShouldReturn200() throws Exception {
        when(itemClient.deleteItem(anyLong(), anyLong())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }
}
