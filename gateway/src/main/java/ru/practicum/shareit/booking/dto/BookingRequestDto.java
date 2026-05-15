package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDto {

    @NotNull(message = "ItemId cannot be null")
    private Long itemId;

    @NotNull(message = "Start time cannot be null")
    @FutureOrPresent(message = "Start must be in present or future")
    private LocalDateTime start;

    @NotNull(message = "End time cannot be null")
    @Future(message = "End must be in future")
    private LocalDateTime end;
}
