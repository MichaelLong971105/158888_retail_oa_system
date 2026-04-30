package com.retail.oa.repository;

import com.retail.oa.entity.LeaveRequest;
import com.retail.oa.entity.LeaveRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Repository for leave request persistence.
 */
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    List<LeaveRequest> findByStatusOrderByCreatedAtAsc(LeaveRequestStatus status);

    List<LeaveRequest> findByStatusAndApproverIdOrderByCreatedAtAsc(LeaveRequestStatus status, Long approverId);

    List<LeaveRequest> findByApplicantIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long applicantId,
            Collection<LeaveRequestStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    List<LeaveRequest> findByApplicantIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Long applicantId,
            LeaveRequestStatus status,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
}
