package com.retail.oa.service;

import com.retail.oa.dto.attendance.AttendancePunchRecordResponse;
import com.retail.oa.dto.attendance.AttendanceEmployeeOptionResponse;
import com.retail.oa.dto.attendance.LeaveApprovalRequest;
import com.retail.oa.dto.attendance.LeaveRequestCreateRequest;
import com.retail.oa.dto.attendance.LeaveRequestResponse;
import com.retail.oa.dto.attendance.PunchRecordIngestRequest;
import com.retail.oa.dto.attendance.ScheduleShiftRequest;
import com.retail.oa.dto.attendance.WeeklyAttendanceSummaryResponse;
import com.retail.oa.dto.attendance.WeeklyScheduleUpsertRequest;
import com.retail.oa.dto.attendance.WorkScheduleResponse;
import com.retail.oa.entity.AttendancePunchRecord;
import com.retail.oa.entity.AttendancePunchType;
import com.retail.oa.entity.LeaveRequest;
import com.retail.oa.entity.LeaveRequestStatus;
import com.retail.oa.entity.ShiftType;
import com.retail.oa.entity.User;
import com.retail.oa.entity.UserPermission;
import com.retail.oa.entity.UserRole;
import com.retail.oa.entity.WorkSchedule;
import com.retail.oa.exception.InvalidOperationException;
import com.retail.oa.exception.ResourceNotFoundException;
import com.retail.oa.repository.AttendancePunchRecordRepository;
import com.retail.oa.repository.LeaveRequestRepository;
import com.retail.oa.repository.UserRepository;
import com.retail.oa.repository.WorkScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles scheduling, leave approval, punch ingestion and weekly summaries.
 */
@Service
@Transactional
public class AttendanceService {

    private final UserRepository userRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendancePunchRecordRepository attendancePunchRecordRepository;

    public AttendanceService(
            UserRepository userRepository,
            WorkScheduleRepository workScheduleRepository,
            LeaveRequestRepository leaveRequestRepository,
            AttendancePunchRecordRepository attendancePunchRecordRepository
    ) {
        this.userRepository = userRepository;
        this.workScheduleRepository = workScheduleRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.attendancePunchRecordRepository = attendancePunchRecordRepository;
    }

    public List<WorkScheduleResponse> upsertWeeklySchedule(WeeklyScheduleUpsertRequest request, String operatorUsername) {
        User operator = getUserByUsername(operatorUsername);
        ensureCanManageAttendance(operator);

        User employee = getUserById(request.getEmployeeId());
        if (employee.getRole() != UserRole.STAFF) {
            throw new InvalidOperationException("Only staff accounts can be scheduled in the attendance module");
        }

        LocalDate normalizedWeekStart = normalizeWeekStart(request.getWeekStartDate());
        if (!normalizedWeekStart.equals(request.getWeekStartDate())) {
            throw new InvalidOperationException("Week start date must be a Monday");
        }

        validateScheduleRequest(normalizedWeekStart, request.getShifts());

        return request.getShifts().stream()
                .map(shiftRequest -> saveSchedule(employee, operator, normalizedWeekStart, shiftRequest))
                .map(this::toWorkScheduleResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkScheduleResponse> getWeeklySchedule(Long employeeId, LocalDate weekStartDate, String operatorUsername) {
        User operator = getUserByUsername(operatorUsername);
        User employee = resolveScheduleTarget(operator, employeeId);
        LocalDate normalizedWeekStart = normalizeWeekStart(weekStartDate);

        return workScheduleRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
                        employee.getId(),
                        normalizedWeekStart,
                        normalizedWeekStart.plusDays(6)
                ).stream()
                .map(this::toWorkScheduleResponse)
                .collect(Collectors.toList());
    }

    public LeaveRequestResponse createLeaveRequest(LeaveRequestCreateRequest request, String applicantUsername) {
        User applicant = getUserByUsername(applicantUsername);
        validateLeaveWindow(request.getStartTime(), request.getEndTime());
        ensureNoLeaveOverlap(applicant.getId(), request.getStartTime(), request.getEndTime());

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setApplicant(applicant);
        if (request.getApproverId() != null) {
            leaveRequest.setApprover(resolveApprover(request.getApproverId()));
        }
        leaveRequest.setLeaveType(request.getLeaveType());
        leaveRequest.setStartTime(request.getStartTime());
        leaveRequest.setEndTime(request.getEndTime());
        leaveRequest.setReason(request.getReason().trim());
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);

        return toLeaveRequestResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getMyLeaveRequests(String applicantUsername) {
        User applicant = getUserByUsername(applicantUsername);
        return leaveRequestRepository.findByApplicantIdOrderByCreatedAtDesc(applicant.getId())
                .stream()
                .map(this::toLeaveRequestResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getPendingLeaveRequests(String approverUsername) {
        User approver = getUserByUsername(approverUsername);
        ensureCanApproveLeave(approver);
        List<LeaveRequest> pendingRequests = approver.getRole() == UserRole.ADMIN
                ? leaveRequestRepository.findByStatusOrderByCreatedAtAsc(LeaveRequestStatus.PENDING)
                : leaveRequestRepository.findByStatusOrderByCreatedAtAsc(LeaveRequestStatus.PENDING).stream()
                .filter(request -> request.getApprover() == null || request.getApprover().getId().equals(approver.getId()))
                .collect(Collectors.toList());

        return pendingRequests
                .stream()
                .map(this::toLeaveRequestResponse)
                .collect(Collectors.toList());
    }

    public LeaveRequestResponse approveLeaveRequest(Long leaveRequestId, LeaveApprovalRequest request, String approverUsername) {
        User approver = getUserByUsername(approverUsername);
        ensureCanApproveLeave(approver);

        LeaveRequest leaveRequest = getLeaveRequestById(leaveRequestId);
        if (leaveRequest.getApplicant().getId().equals(approver.getId())) {
            throw new InvalidOperationException("Applicants cannot approve their own leave requests");
        }
        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new InvalidOperationException("Only pending leave requests can be approved");
        }

        ensureNoScheduleConflictForApprovedLeave(
                leaveRequest.getApplicant().getId(),
                leaveRequest.getStartTime(),
                leaveRequest.getEndTime()
        );

        leaveRequest.setApprover(approver);
        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest.setApprovalComment(trimToNull(request == null ? null : request.getComment()));
        leaveRequest.setApprovedAt(LocalDateTime.now());

        return toLeaveRequestResponse(leaveRequestRepository.save(leaveRequest));
    }

    public LeaveRequestResponse rejectLeaveRequest(Long leaveRequestId, LeaveApprovalRequest request, String approverUsername) {
        User approver = getUserByUsername(approverUsername);
        ensureCanApproveLeave(approver);

        LeaveRequest leaveRequest = getLeaveRequestById(leaveRequestId);
        if (leaveRequest.getApplicant().getId().equals(approver.getId())) {
            throw new InvalidOperationException("Applicants cannot reject their own leave requests");
        }
        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new InvalidOperationException("Only pending leave requests can be rejected");
        }

        leaveRequest.setApprover(approver);
        leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
        leaveRequest.setApprovalComment(trimToNull(request == null ? null : request.getComment()));
        leaveRequest.setApprovedAt(LocalDateTime.now());

        return toLeaveRequestResponse(leaveRequestRepository.save(leaveRequest));
    }

    public LeaveRequestResponse cancelLeaveRequest(Long leaveRequestId, String applicantUsername) {
        User applicant = getUserByUsername(applicantUsername);
        LeaveRequest leaveRequest = getLeaveRequestById(leaveRequestId);

        if (!leaveRequest.getApplicant().getId().equals(applicant.getId())) {
            throw new InvalidOperationException("You can only cancel your own leave requests");
        }
        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new InvalidOperationException("Only pending leave requests can be cancelled");
        }

        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);
        return toLeaveRequestResponse(leaveRequestRepository.save(leaveRequest));
    }

    public AttendancePunchRecordResponse ingestPunchRecord(PunchRecordIngestRequest request, String operatorUsername) {
        User operator = getUserByUsername(operatorUsername);
        ensureCanManageAttendance(operator);

        User employee = getUserById(request.getEmployeeId());
        if (request.getExternalRecordId() != null && !request.getExternalRecordId().isBlank()) {
            return attendancePunchRecordRepository.findByExternalRecordId(request.getExternalRecordId().trim())
                    .map(this::toPunchRecordResponse)
                    .orElseGet(() -> toPunchRecordResponse(attendancePunchRecordRepository.save(buildPunchRecord(employee, request))));
        }

        return toPunchRecordResponse(attendancePunchRecordRepository.save(buildPunchRecord(employee, request)));
    }

    @Transactional(readOnly = true)
    public WeeklyAttendanceSummaryResponse getWeeklySummary(Long employeeId, LocalDate weekStartDate, String operatorUsername) {
        User operator = getUserByUsername(operatorUsername);
        User employee = resolveScheduleTarget(operator, employeeId);
        LocalDate normalizedWeekStart = normalizeWeekStart(weekStartDate);
        LocalDate weekEndDate = normalizedWeekStart.plusDays(6);
        LocalDateTime weekStartDateTime = normalizedWeekStart.atStartOfDay();
        LocalDateTime weekEndExclusive = normalizedWeekStart.plusDays(7).atStartOfDay();

        List<WorkSchedule> schedules = workScheduleRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
                employee.getId(),
                normalizedWeekStart,
                weekEndDate
        );
        List<AttendancePunchRecord> punches = attendancePunchRecordRepository.findByEmployeeIdAndPunchTimeBetweenOrderByPunchTimeAsc(
                employee.getId(),
                weekStartDateTime,
                weekEndExclusive
        );
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findByApplicantIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                employee.getId(),
                LeaveRequestStatus.APPROVED,
                weekEndExclusive,
                weekStartDateTime
        );

        long scheduledMinutes = schedules.stream()
                .filter(schedule -> schedule.getShiftType() == ShiftType.WORK)
                .mapToLong(this::calculateScheduledMinutes)
                .sum();
        long actualMinutes = calculateActualWorkedMinutes(punches);
        long approvedLeaveMinutes = calculateApprovedLeaveMinutes(schedules, approvedLeaves);

        WeeklyAttendanceSummaryResponse response = new WeeklyAttendanceSummaryResponse();
        response.setEmployeeId(employee.getId());
        response.setEmployeeUsername(employee.getUsername());
        response.setWeekStartDate(normalizedWeekStart);
        response.setWeekEndDate(weekEndDate);
        response.setScheduledHours(toHours(scheduledMinutes));
        response.setActualHours(toHours(actualMinutes));
        response.setApprovedLeaveHours(toHours(approvedLeaveMinutes));
        response.setPayableHours(toHours(actualMinutes + approvedLeaveMinutes));
        return response;
    }

    @Transactional(readOnly = true)
    public List<WeeklyAttendanceSummaryResponse> getTeamWeeklySummary(LocalDate weekStartDate, String operatorUsername) {
        User operator = getUserByUsername(operatorUsername);
        ensureCanManageAttendance(operator);

        return userRepository.findByRoleInAndEnabledTrue(List.of(UserRole.STAFF)).stream()
                .map(user -> getWeeklySummary(user.getId(), weekStartDate, operatorUsername))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceEmployeeOptionResponse> getAttendanceEmployees(String operatorUsername) {
        User operator = getUserByUsername(operatorUsername);

        if (operator.getRole() == UserRole.STAFF
                && !operator.getAdditionalPermissions().contains(UserPermission.VIEW_ATTENDANCE)
                && !operator.getAdditionalPermissions().contains(UserPermission.MANAGE_ATTENDANCE)) {
            return List.of(toAttendanceEmployeeOption(operator));
        }

        return userRepository.findByRoleInAndEnabledTrue(List.of(UserRole.STAFF)).stream()
                .map(this::toAttendanceEmployeeOption)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceEmployeeOptionResponse> getAttendanceApprovers(String operatorUsername) {
        getUserByUsername(operatorUsername);

        return userRepository.findByRoleInAndEnabledTrue(List.of(UserRole.ADMIN, UserRole.MANAGER)).stream()
                .map(this::toAttendanceEmployeeOption)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendancePunchRecordResponse> getPunchRecords(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            String operatorUsername
    ) {
        User operator = getUserByUsername(operatorUsername);
        User employee = resolveScheduleTarget(operator, employeeId);

        LocalDate queryStart = startDate == null ? LocalDate.now().minusDays(6) : startDate;
        LocalDate queryEnd = endDate == null ? queryStart.plusDays(6) : endDate;
        if (queryEnd.isBefore(queryStart)) {
            throw new InvalidOperationException("End date must not be earlier than start date");
        }

        return attendancePunchRecordRepository.findByEmployeeIdAndPunchTimeBetweenOrderByPunchTimeAsc(
                        employee.getId(),
                        queryStart.atStartOfDay(),
                        queryEnd.plusDays(1).atStartOfDay()
                ).stream()
                .map(this::toPunchRecordResponse)
                .collect(Collectors.toList());
    }

    private AttendancePunchRecord buildPunchRecord(User employee, PunchRecordIngestRequest request) {
        AttendancePunchRecord record = new AttendancePunchRecord();
        record.setEmployee(employee);
        record.setPunchType(request.getPunchType());
        record.setSource(request.getSource());
        record.setPunchTime(request.getPunchTime());
        record.setExternalRecordId(trimToNull(request.getExternalRecordId()));
        record.setDeviceCode(trimToNull(request.getDeviceCode()));
        record.setRawPayload(trimToNull(request.getRawPayload()));
        return record;
    }

    private WorkSchedule saveSchedule(User employee, User operator, LocalDate weekStartDate, ScheduleShiftRequest request) {
        WorkSchedule schedule = workScheduleRepository.findByEmployeeIdAndWorkDate(employee.getId(), request.getWorkDate())
                .orElseGet(WorkSchedule::new);

        if (request.getShiftType() == ShiftType.WORK) {
            ensureNoApprovedLeaveConflict(employee.getId(), request.getWorkDate(), request.getStartTime(), request.getEndTime());
        }

        schedule.setEmployee(employee);
        schedule.setAssignedBy(operator);
        schedule.setWeekStartDate(weekStartDate);
        schedule.setWorkDate(request.getWorkDate());
        schedule.setShiftType(request.getShiftType());
        schedule.setNote(trimToNull(request.getNote()));

        if (request.getShiftType() == ShiftType.REST) {
            schedule.setStartTime(null);
            schedule.setEndTime(null);
            schedule.setBreakMinutes(0);
        } else {
            schedule.setStartTime(request.getStartTime());
            schedule.setEndTime(request.getEndTime());
            schedule.setBreakMinutes(defaultBreakMinutes(request.getBreakMinutes()));
        }

        return workScheduleRepository.save(schedule);
    }

    private void validateScheduleRequest(LocalDate weekStartDate, List<ScheduleShiftRequest> shifts) {
        Set<LocalDate> seenDates = new HashSet<>();

        for (ScheduleShiftRequest shift : shifts) {
            if (shift.getWorkDate().isBefore(weekStartDate) || shift.getWorkDate().isAfter(weekStartDate.plusDays(6))) {
                throw new InvalidOperationException("Shift date must stay within the requested week");
            }
            if (!seenDates.add(shift.getWorkDate())) {
                throw new InvalidOperationException("Duplicate shift date found in weekly schedule request");
            }

            if (shift.getShiftType() == ShiftType.REST) {
                if (shift.getStartTime() != null || shift.getEndTime() != null) {
                    throw new InvalidOperationException("Rest days cannot include start or end time");
                }
                continue;
            }

            if (shift.getStartTime() == null || shift.getEndTime() == null) {
                throw new InvalidOperationException("Work shifts must include start and end time");
            }
            if (!shift.getEndTime().isAfter(shift.getStartTime())) {
                throw new InvalidOperationException("Work shift end time must be after start time");
            }

            long shiftMinutes = Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();
            int breakMinutes = defaultBreakMinutes(shift.getBreakMinutes());
            if (breakMinutes >= shiftMinutes) {
                throw new InvalidOperationException("Break minutes must be shorter than the work shift duration");
            }
        }
    }

    private void validateLeaveWindow(LocalDateTime startTime, LocalDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new InvalidOperationException("Leave end time must be after start time");
        }
    }

    private void ensureNoLeaveOverlap(Long applicantId, LocalDateTime startTime, LocalDateTime endTime) {
        List<LeaveRequestStatus> blockingStatuses = List.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED);
        boolean hasOverlap = !leaveRequestRepository.findByApplicantIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                applicantId,
                blockingStatuses,
                endTime,
                startTime
        ).isEmpty();

        if (hasOverlap) {
            throw new InvalidOperationException("The leave request overlaps an existing pending or approved request");
        }
    }

    private void ensureNoScheduleConflictForApprovedLeave(Long employeeId, LocalDateTime startTime, LocalDateTime endTime) {
        List<WorkSchedule> schedules = workScheduleRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
                employeeId,
                startTime.toLocalDate(),
                endTime.toLocalDate()
        );

        boolean hasConflict = schedules.stream()
                .filter(schedule -> schedule.getShiftType() == ShiftType.WORK)
                .anyMatch(schedule -> overlaps(schedule, startTime, endTime));

        if (hasConflict) {
            throw new InvalidOperationException("Approved leave cannot overlap an existing work schedule");
        }
    }

    private void ensureNoApprovedLeaveConflict(Long employeeId, LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        LocalDateTime shiftStart = LocalDateTime.of(workDate, startTime);
        LocalDateTime shiftEnd = LocalDateTime.of(workDate, endTime);
        boolean hasConflict = !leaveRequestRepository.findByApplicantIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                employeeId,
                LeaveRequestStatus.APPROVED,
                shiftEnd,
                shiftStart
        ).isEmpty();

        if (hasConflict) {
            throw new InvalidOperationException("Work schedules cannot overlap approved leave");
        }
    }

    private long calculateScheduledMinutes(WorkSchedule schedule) {
        if (schedule.getShiftType() != ShiftType.WORK || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return 0L;
        }

        long shiftMinutes = Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes();
        return shiftMinutes - defaultBreakMinutes(schedule.getBreakMinutes());
    }

    private long calculateActualWorkedMinutes(List<AttendancePunchRecord> punches) {
        long totalMinutes = 0L;
        LocalDateTime currentClockIn = null;

        for (AttendancePunchRecord punch : punches) {
            if (punch.getPunchType() == AttendancePunchType.CLOCK_IN) {
                currentClockIn = punch.getPunchTime();
                continue;
            }

            if (currentClockIn != null && punch.getPunchTime().isAfter(currentClockIn)) {
                totalMinutes += Duration.between(currentClockIn, punch.getPunchTime()).toMinutes();
                currentClockIn = null;
            }
        }

        return totalMinutes;
    }

    private long calculateApprovedLeaveMinutes(List<WorkSchedule> schedules, List<LeaveRequest> approvedLeaves) {
        long totalMinutes = 0L;

        for (WorkSchedule schedule : schedules) {
            if (schedule.getShiftType() != ShiftType.WORK || schedule.getStartTime() == null || schedule.getEndTime() == null) {
                continue;
            }

            LocalDateTime scheduleStart = LocalDateTime.of(schedule.getWorkDate(), schedule.getStartTime());
            LocalDateTime scheduleEnd = LocalDateTime.of(schedule.getWorkDate(), schedule.getEndTime());

            for (LeaveRequest leaveRequest : approvedLeaves) {
                LocalDateTime overlapStart = max(scheduleStart, leaveRequest.getStartTime());
                LocalDateTime overlapEnd = min(scheduleEnd, leaveRequest.getEndTime());

                if (overlapEnd.isAfter(overlapStart)) {
                    totalMinutes += Duration.between(overlapStart, overlapEnd).toMinutes();
                }
            }
        }

        return totalMinutes;
    }

    private boolean overlaps(WorkSchedule schedule, LocalDateTime startTime, LocalDateTime endTime) {
        if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return false;
        }

        LocalDateTime scheduleStart = LocalDateTime.of(schedule.getWorkDate(), schedule.getStartTime());
        LocalDateTime scheduleEnd = LocalDateTime.of(schedule.getWorkDate(), schedule.getEndTime());
        return scheduleStart.isBefore(endTime) && scheduleEnd.isAfter(startTime);
    }

    private User resolveScheduleTarget(User operator, Long requestedEmployeeId) {
        if (requestedEmployeeId == null) {
            return operator;
        }

        if (operator.getRole() == UserRole.STAFF && !operator.getId().equals(requestedEmployeeId)
                && !operator.getAdditionalPermissions().contains(UserPermission.VIEW_ATTENDANCE)
                && !operator.getAdditionalPermissions().contains(UserPermission.MANAGE_ATTENDANCE)) {
            throw new InvalidOperationException("Staff can only view their own attendance data");
        }

        return getUserById(requestedEmployeeId);
    }

    private User resolveApprover(Long approverId) {
        User approver = getUserById(approverId);
        if (approver.getRole() != UserRole.ADMIN && approver.getRole() != UserRole.MANAGER) {
            throw new InvalidOperationException("Selected approver must be an admin or manager");
        }
        return approver;
    }

    private void ensureCanManageAttendance(User user) {
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.MANAGER) {
            return;
        }
        if (user.getAdditionalPermissions().contains(UserPermission.MANAGE_ATTENDANCE)) {
            return;
        }
        throw new InvalidOperationException("Current user cannot manage attendance");
    }

    private void ensureCanApproveLeave(User user) {
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.MANAGER) {
            return;
        }
        if (user.getAdditionalPermissions().contains(UserPermission.APPROVE_LEAVE)
                || user.getAdditionalPermissions().contains(UserPermission.MANAGE_ATTENDANCE)) {
            return;
        }
        throw new InvalidOperationException("Current user cannot approve leave requests");
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private LeaveRequest getLeaveRequestById(Long leaveRequestId) {
        return leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + leaveRequestId));
    }

    private WorkScheduleResponse toWorkScheduleResponse(WorkSchedule schedule) {
        WorkScheduleResponse response = new WorkScheduleResponse();
        response.setId(schedule.getId());
        response.setEmployeeId(schedule.getEmployee().getId());
        response.setEmployeeUsername(schedule.getEmployee().getUsername());
        response.setAssignedById(schedule.getAssignedBy().getId());
        response.setAssignedByUsername(schedule.getAssignedBy().getUsername());
        response.setWeekStartDate(schedule.getWeekStartDate());
        response.setWorkDate(schedule.getWorkDate());
        response.setShiftType(schedule.getShiftType());
        response.setStartTime(schedule.getStartTime());
        response.setEndTime(schedule.getEndTime());
        response.setBreakMinutes(schedule.getBreakMinutes());
        response.setNote(schedule.getNote());
        response.setCreatedAt(schedule.getCreatedAt());
        response.setUpdatedAt(schedule.getUpdatedAt());
        return response;
    }

    private LeaveRequestResponse toLeaveRequestResponse(LeaveRequest leaveRequest) {
        LeaveRequestResponse response = new LeaveRequestResponse();
        response.setId(leaveRequest.getId());
        response.setApplicantId(leaveRequest.getApplicant().getId());
        response.setApplicantUsername(leaveRequest.getApplicant().getUsername());
        if (leaveRequest.getApprover() != null) {
            response.setApproverId(leaveRequest.getApprover().getId());
            response.setApproverUsername(leaveRequest.getApprover().getUsername());
        }
        response.setLeaveType(leaveRequest.getLeaveType());
        response.setStartTime(leaveRequest.getStartTime());
        response.setEndTime(leaveRequest.getEndTime());
        response.setReason(leaveRequest.getReason());
        response.setStatus(leaveRequest.getStatus());
        response.setApprovalComment(leaveRequest.getApprovalComment());
        response.setRequestedAt(leaveRequest.getRequestedAt());
        response.setApprovedAt(leaveRequest.getApprovedAt());
        response.setCreatedAt(leaveRequest.getCreatedAt());
        response.setUpdatedAt(leaveRequest.getUpdatedAt());
        return response;
    }

    private AttendancePunchRecordResponse toPunchRecordResponse(AttendancePunchRecord record) {
        AttendancePunchRecordResponse response = new AttendancePunchRecordResponse();
        response.setId(record.getId());
        response.setEmployeeId(record.getEmployee().getId());
        response.setEmployeeUsername(record.getEmployee().getUsername());
        response.setPunchType(record.getPunchType());
        response.setSource(record.getSource());
        response.setPunchTime(record.getPunchTime());
        response.setExternalRecordId(record.getExternalRecordId());
        response.setDeviceCode(record.getDeviceCode());
        response.setRawPayload(record.getRawPayload());
        response.setCreatedAt(record.getCreatedAt());
        return response;
    }

    private AttendanceEmployeeOptionResponse toAttendanceEmployeeOption(User user) {
        AttendanceEmployeeOptionResponse response = new AttendanceEmployeeOptionResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }

    private LocalDate normalizeWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private int defaultBreakMinutes(Integer breakMinutes) {
        return breakMinutes == null ? 0 : breakMinutes;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalDateTime max(LocalDateTime first, LocalDateTime second) {
        return first.isAfter(second) ? first : second;
    }

    private LocalDateTime min(LocalDateTime first, LocalDateTime second) {
        return first.isBefore(second) ? first : second;
    }

    private BigDecimal toHours(long minutes) {
        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
