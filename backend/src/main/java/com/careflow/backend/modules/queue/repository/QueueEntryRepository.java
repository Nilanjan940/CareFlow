package com.careflow.backend.modules.queue.repository;

import com.careflow.backend.modules.queue.entity.QueueEntry;
import com.careflow.backend.modules.queue.entity.QueueStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface QueueEntryRepository
    extends JpaRepository<QueueEntry, UUID> {

    @Query("""
        SELECT COALESCE(MAX(q.queueNumber), 0)
        FROM QueueEntry q
        WHERE q.doctor.id = :doctorId
          AND q.queueDate = :queueDate
        """)
    Integer findMaximumQueueNumber(
        @Param("doctorId") UUID doctorId,
        @Param("queueDate") LocalDate queueDate
    );

    boolean existsByAppointmentIdAndClinicId(
        UUID appointmentId,
        UUID clinicId
    );

    Optional<QueueEntry> findByIdAndClinicId(
        UUID id,
        UUID clinicId
    );

    @Query(
        value = """
            SELECT q FROM QueueEntry q
            WHERE q.clinic.id = :clinicId
              AND q.doctor.id = :doctorId
              AND q.queueDate = :queueDate
              AND (:status IS NULL OR q.status = :status)
            """,
        countQuery = """
            SELECT COUNT(q) FROM QueueEntry q
            WHERE q.clinic.id = :clinicId
              AND q.doctor.id = :doctorId
              AND q.queueDate = :queueDate
              AND (:status IS NULL OR q.status = :status)
            """
    )
    Page<QueueEntry> searchQueue(
        @Param("clinicId") UUID clinicId,
        @Param("doctorId") UUID doctorId,
        @Param("queueDate") LocalDate queueDate,
        @Param("status") QueueStatus status,
        Pageable pageable
    );
}