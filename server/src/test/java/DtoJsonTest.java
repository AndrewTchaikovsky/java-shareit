import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItServer.class)
public class DtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> commentJson;

    @Autowired
    private JacksonTester<ItemRequestDto> requestJson;

    @Test
    void commentDtoShouldSerializeCreatedDate() throws Exception {
        CommentDto dto = new CommentDto(
                1L,
                "Text",
                "User",
                LocalDateTime.of(2026, 1, 1, 12, 0)
        );

        JsonContent<CommentDto> result = commentJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-01-01T12:00:00");
    }

    @Test
    void itemRequestDtoShouldSerializeCreatedDate() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need item")
                .created(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();

        JsonContent<ItemRequestDto> result = requestJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-01-01T12:00:00");
    }
}
