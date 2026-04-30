package com.retail.oa.dto.attendance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Batch schedule request for one employee and one week.
 */
@Getter
@Setter
public class WeeklyScheduleUpsertRequest {

    @NotNull(message = "Employee id cannot be null")
    private Long employeeId;

    @NotNull(message = "Week start date cannot be null")
    private LocalDate weekStartDate;

    @Valid
    @NotEmpty(message = "At least one shift must be provided")
    private List<ScheduleShiftRequest> shifts = new ArrayList<>();
}
