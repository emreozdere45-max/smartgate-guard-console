package com.smartgate.backend.dto;

import java.time.LocalDateTime;

public record GateLogResponse(
    Long id,
    LocalDateTime eventTime,
    String method,
    String doorId,
    String note
) {
}

