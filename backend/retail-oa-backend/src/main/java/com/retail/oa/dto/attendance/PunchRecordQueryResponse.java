package com.retail.oa.dto.attendance;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Punch history list wrapper.
 */
@Getter
@Setter
public class PunchRecordQueryResponse {

    private List<AttendancePunchRecordResponse> records = new ArrayList<>();
}
