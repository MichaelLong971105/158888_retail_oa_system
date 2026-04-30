package com.retail.oa.dto.attendance;

import com.retail.oa.entity.ShiftType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request payload for one scheduled day.
 */
@Getter
@Setter
public class ScheduleShiftRequest {

    @NotNull(message = "Work date cannot be null")
    private LocalDate workDate;

    @NotNull(message = "Shift type cannot be null")
    private ShiftType shiftType;

    private LocalTime startTime;

    private LocalTime endTime;

    @Min(value = 0, message = "Break minutes cannot be negative")
    @Max(value = 720, message = "Break minutes is too large")
    private Integer breakMinutes = 0;

    private String note;
}
