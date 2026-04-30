package com.retail.oa.dto.attendance;

import com.retail.oa.entity.LeaveRequestStatus;
import com.retail.oa.entity.LeaveType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * API response for leave requests.
 */
@Getter
@Setter
public class LeaveRequestResponse {

    private Long id;
    private Long applicantId;
    private String applicantUsername;
    private Long approverId;
    private String approverUsername;
    private LeaveType leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private LeaveRequestStatus status;
    private String approvalComment;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
