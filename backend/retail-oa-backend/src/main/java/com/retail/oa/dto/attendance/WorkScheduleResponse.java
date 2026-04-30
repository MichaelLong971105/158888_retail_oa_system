package com.retail.oa.dto.attendance;

import com.retail.oa.entity.ShiftType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * API response for work schedules.
 */
@Getter
@Setter
public class WorkScheduleResponse {

    private Long id;
    private Long employeeId;
    private String employeeUsername;
    private Long assignedById;
    private String assignedByUsername;
    private LocalDate weekStartDate;
    private LocalDate workDate;
    private ShiftType shiftType;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakMinutes;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
