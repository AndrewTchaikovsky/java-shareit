import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@ContextConfiguration(classes = ShareItServer.class)
public class BookingControllerServerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBookingShouldReturn200() throws Exception {
        BookingRequestDto dto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        BookingResponseDto response = new BookingResponseDto();
        response.setId(1L);
        response.setStatus(Status.WAITING);
        when(bookingService.create(anyLong(), any())).thenReturn(response);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void approveBookingShouldReturn200() throws Exception {
        BookingResponseDto response = new BookingResponseDto();
        response.setId(1L);
        response.setStatus(Status.APPROVED);
        when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(response);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingShouldReturn200() throws Exception {
        BookingResponseDto response = new BookingResponseDto();
        response.setId(1L);
        when(bookingService.get(anyLong(), anyLong())).thenReturn(response);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getBookingsShouldReturn200() throws Exception {
        BookingResponseDto response = new BookingResponseDto();
        response.setId(1L);
        when(bookingService.getAll(anyLong(), any(State.class), anyInt(), anyInt()))
                .thenReturn(List.of(response));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getOwnerBookingsShouldReturn200() throws Exception {
        BookingResponseDto response = new BookingResponseDto();
        response.setId(1L);
        when(bookingService.getOwnerBookings(anyLong(), any(State.class), anyInt(), anyInt()))
                .thenReturn(List.of(response));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}