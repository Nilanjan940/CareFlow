package com.careflow.backend.modules.queue.dto;

import java.util.List;

public record QueuePageResponse(
    List<QueueEntryResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {}