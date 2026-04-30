package com.retail.oa.repository;

import com.retail.oa.entity.AttendancePunchRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for raw attendance punch events.
 */
public interface AttendancePunchRecordRepository extends JpaRepository<AttendancePunchRecord, Long> {

    Optional<AttendancePunchRecord> findByExternalRecordId(String externalRecordId);

    List<AttendancePunchRecord> findByEmployeeIdAndPunchTimeBetweenOrderByPunchTimeAsc(
            Long employeeId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}
