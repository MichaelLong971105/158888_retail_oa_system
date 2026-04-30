package com.retail.oa.dto.attendance;

import com.retail.oa.entity.LeaveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Payload for submitting a leave request.
 */
@Getter
@Setter
public class LeaveRequestCreateRequest {

    private Long approverId;

    @NotNull(message = "Leave type cannot be null")
    private LeaveType leaveType;

    @NotNull(message = "Start time cannot be null")
    private LocalDateTime startTime;

    @NotNull(message = "End time cannot be null")
    private LocalDateTime endTime;

    @NotBlank(message = "Reason cannot be empty")
    private String reason;
}
