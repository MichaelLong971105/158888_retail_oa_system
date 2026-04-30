package com.retail.oa.dto.attendance;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Weekly attendance summary for payroll or reporting.
 */
@Getter
@Setter
public class WeeklyAttendanceSummaryResponse {

    private Long employeeId;
    private String employeeUsername;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private BigDecimal scheduledHours;
    private BigDecimal actualHours;
    private BigDecimal approvedLeaveHours;
    private BigDecimal payableHours;
}
