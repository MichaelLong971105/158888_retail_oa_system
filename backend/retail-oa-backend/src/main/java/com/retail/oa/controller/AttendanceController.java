package com.retail.oa.controller;

import com.retail.oa.dto.attendance.AttendancePunchRecordResponse;
import com.retail.oa.dto.attendance.AttendanceEmployeeOptionResponse;
import com.retail.oa.dto.attendance.LeaveApprovalRequest;
import com.retail.oa.dto.attendance.LeaveRequestCreateRequest;
import com.retail.oa.dto.attendance.LeaveRequestResponse;
import com.retail.oa.dto.attendance.PunchRecordIngestRequest;
import com.retail.oa.dto.attendance.WeeklyAttendanceSummaryResponse;
import com.retail.oa.dto.attendance.WeeklyScheduleUpsertRequest;
import com.retail.oa.dto.attendance.WorkScheduleResponse;
import com.retail.oa.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Attendance module APIs for schedules, leave, punch ingestion and weekly summaries.
 */
@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PutMapping("/schedules/week")
    public List<WorkScheduleResponse> upsertWeeklySchedule(
            @Valid @RequestBody WeeklyScheduleUpsertRequest request,
            Authentication authentication
    ) {
        return attendanceService.upsertWeeklySchedule(request, authentication.getName());
    }

    @GetMapping("/schedules/week")
    public List<WorkScheduleResponse> getWeeklySchedule(
            @RequestParam(required = false) Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            Authentication authentication
    ) {
        return attendanceService.getWeeklySchedule(employeeId, weekStartDate, authentication.getName());
    }

    @PostMapping("/leave-requests")
    public LeaveRequestResponse createLeaveRequest(
            @Valid @RequestBody LeaveRequestCreateRequest request,
            Authentication authentication
    ) {
        return attendanceService.createLeaveRequest(request, authentication.getName());
    }

    @GetMapping("/leave-requests/mine")
    public List<LeaveRequestResponse> getMyLeaveRequests(Authentication authentication) {
        return attendanceService.getMyLeaveRequests(authentication.getName());
    }

    @GetMapping("/leave-requests/pending")
    public List<LeaveRequestResponse> getPendingLeaveRequests(Authentication authentication) {
        return attendanceService.getPendingLeaveRequests(authentication.getName());
    }

    @PostMapping("/leave-requests/{id}/approve")
    public ResponseEntity<LeaveRequestResponse> approveLeaveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveApprovalRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(attendanceService.approveLeaveRequest(id, request, authentication.getName()));
    }

    @PostMapping("/leave-requests/{id}/reject")
    public ResponseEntity<LeaveRequestResponse> rejectLeaveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveApprovalRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(attendanceService.rejectLeaveRequest(id, request, authentication.getName()));
    }

    @PostMapping("/leave-requests/{id}/cancel")
    public ResponseEntity<LeaveRequestResponse> cancelLeaveRequest(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(attendanceService.cancelLeaveRequest(id, authentication.getName()));
    }

    @PostMapping("/punch-records")
    public AttendancePunchRecordResponse ingestPunchRecord(
            @Valid @RequestBody PunchRecordIngestRequest request,
            Authentication authentication
    ) {
        return attendanceService.ingestPunchRecord(request, authentication.getName());
    }

    @GetMapping("/punch-records")
    public List<AttendancePunchRecordResponse> getPunchRecords(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        return attendanceService.getPunchRecords(employeeId, startDate, endDate, authentication.getName());
    }

    @GetMapping("/weekly-summary")
    public WeeklyAttendanceSummaryResponse getWeeklySummary(
            @RequestParam(required = false) Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            Authentication authentication
    ) {
        return attendanceService.getWeeklySummary(employeeId, weekStartDate, authentication.getName());
    }

    @GetMapping("/weekly-summary/team")
    public List<WeeklyAttendanceSummaryResponse> getTeamWeeklySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            Authentication authentication
    ) {
        return attendanceService.getTeamWeeklySummary(weekStartDate, authentication.getName());
    }

    @GetMapping("/employees")
    public List<AttendanceEmployeeOptionResponse> getAttendanceEmployees(Authentication authentication) {
        return attendanceService.getAttendanceEmployees(authentication.getName());
    }

    @GetMapping("/approvers")
    public List<AttendanceEmployeeOptionResponse> getAttendanceApprovers(Authentication authentication) {
        return attendanceService.getAttendanceApprovers(authentication.getName());
    }
}
