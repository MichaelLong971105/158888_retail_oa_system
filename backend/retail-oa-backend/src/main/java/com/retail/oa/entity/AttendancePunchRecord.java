package com.retail.oa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Raw attendance punch event ingested from a device or external system.
 */
@Entity
@Getter
@Setter
@Table(name = "attendance_punch_records")
public class AttendancePunchRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendancePunchSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "punch_type", nullable = false, length = 20)
    private AttendancePunchType punchType;

    @Column(name = "punch_time", nullable = false)
    private LocalDateTime punchTime;

    // Makes punch ingestion idempotent when a time clock retries delivery.
    @Column(name = "external_record_id", unique = true, length = 100)
    private String externalRecordId;

    @Column(name = "device_code", length = 100)
    private String deviceCode;

    @Column(name = "raw_payload", length = 2000)
    private String rawPayload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
