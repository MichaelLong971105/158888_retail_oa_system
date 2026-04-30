package com.retail.oa.dto.attendance;

import lombok.Getter;
import lombok.Setter;

/**
 * Payload for leave approval or rejection.
 */
@Getter
@Setter
public class LeaveApprovalRequest {

    private String comment;
}
