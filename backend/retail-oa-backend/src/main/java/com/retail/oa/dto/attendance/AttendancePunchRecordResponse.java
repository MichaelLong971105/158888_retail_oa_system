package com.retail.oa.dto.attendance;

import com.retail.oa.entity.AttendancePunchSource;
import com.retail.oa.entity.AttendancePunchType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * API response for ingested punch records.
 */
@Getter
@Setter
public class AttendancePunchRecordResponse {

    private Long id;
    private Long employeeId;
    private String employeeUsername;
    private AttendancePunchType punchType;
    private AttendancePunchSource source;
    private LocalDateTime punchTime;
    private String externalRecordId;
    private String deviceCode;
    private String rawPayload;
    private LocalDateTime createdAt;
}
