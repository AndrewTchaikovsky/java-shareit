import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
@ContextConfiguration(classes = ShareItServer.class)
public class BookingRequestDtoTest {

    @Autowired
    private JacksonTester<BookingRequestDto> json;

    @Test
    void testSerialize() throws Exception {

        BookingRequestDto dto = new BookingRequestDto(
                1L,
                LocalDateTime.of(2026, 1, 1, 12, 0),
                LocalDateTime.of(2026, 1, 2, 12, 0)
        );

        JsonContent<BookingRequestDto> result = json.write(dto);

        assertThat(result)
                .extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(1);

        assertThat(result)
                .extractingJsonPathStringValue("$.start")
                .isEqualTo("2026-01-01T12:00:00");

    }

}
