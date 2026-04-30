package com.retail.oa.dto.attendance;

import com.retail.oa.entity.AttendancePunchSource;
import com.retail.oa.entity.AttendancePunchType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Payload for one attendance punch event.
 */
@Getter
@Setter
public class PunchRecordIngestRequest {

    @NotNull(message = "Employee id cannot be null")
    private Long employeeId;

    @NotNull(message = "Punch type cannot be null")
    private AttendancePunchType punchType;

    @NotNull(message = "Punch time cannot be null")
    private LocalDateTime punchTime;

    @NotNull(message = "Source cannot be null")
    private AttendancePunchSource source;

    private String externalRecordId;

    private String deviceCode;

    private String rawPayload;
}
