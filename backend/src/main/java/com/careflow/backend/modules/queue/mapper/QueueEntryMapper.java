package com.careflow.backend.modules.queue.mapper;

import com.careflow.backend.modules.queue.dto.QueueEntryResponse;
import com.careflow.backend.modules.queue.entity.QueueEntry;

public final class QueueEntryMapper {

    private QueueEntryMapper() {
    }

    public static QueueEntryResponse toResponse(QueueEntry entry) {

        return new QueueEntryResponse(
            entry.getId(),
            entry.getClinic().getId(),
            entry.getDoctor().getId(),
            entry.getPatient().getId(),
            entry.getAppointment() != null
                ? entry.getAppointment().getId()
                : null,
            entry.getQueueDate(),
            entry.getQueueNumber(),
            entry.getStatus(),
            entry.getCheckedInAt(),
            entry.getStartedAt(),
            entry.getCompletedAt(),
            entry.getNotes(),
            entry.getCreatedAt()
        );
    }
}