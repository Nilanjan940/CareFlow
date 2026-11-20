package com.careflow.backend.modules.queue.controller;

import com.careflow.backend.modules.queue.dto.QueueCheckInRequest;
import com.careflow.backend.modules.queue.dto.QueueEntryResponse;
import com.careflow.backend.modules.queue.dto.QueuePageResponse;
import com.careflow.backend.modules.queue.dto.QueueStatusUpdateRequest;
import com.careflow.backend.modules.queue.entity.QueueStatus;
import com.careflow.backend.modules.queue.service.QueueService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinics/{clinicId}/queue")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping
    public ResponseEntity<QueueEntryResponse> checkIn(
        @PathVariable UUID clinicId,
        @Valid @RequestBody QueueCheckInRequest request
    ) {

        QueueEntryResponse response =
            queueService.checkIn(clinicId, request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping
    public ResponseEntity<QueuePageResponse> getDailyQueue(
        @PathVariable UUID clinicId,
        @RequestParam UUID doctorId,
        @RequestParam LocalDate date,
        @RequestParam(required = false) QueueStatus status,
        @PageableDefault(
            page = 0,
            size = 10,
            sort = "queueNumber",
            direction = Sort.Direction.ASC
        ) Pageable pageable
    ) {

        return ResponseEntity.ok(
            queueService.searchQueue(
                clinicId,
                doctorId,
                date,
                status,
                pageable
            )
        );
    }

    @GetMapping("/{queueEntryId}")
    public ResponseEntity<QueueEntryResponse> getQueueEntryById(
        @PathVariable UUID clinicId,
        @PathVariable UUID queueEntryId
    ) {

        return ResponseEntity.ok(
            queueService.getQueueEntryById(
                clinicId,
                queueEntryId
            )
        );
    }
    @PatchMapping("/{queueEntryId}/status")
    public ResponseEntity<QueueEntryResponse> updateQueueStatus(
        @PathVariable UUID clinicId,
        @PathVariable UUID queueEntryId,
        @Valid @RequestBody QueueStatusUpdateRequest request
    ) {

        return ResponseEntity.ok(
            queueService.updateStatus(
                clinicId,
                queueEntryId,
                request
            )
        );
    }
}