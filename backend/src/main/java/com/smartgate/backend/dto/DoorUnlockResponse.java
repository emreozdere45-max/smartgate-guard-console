package com.smartgate.backend.dto;

import java.time.LocalDateTime;

public record DoorUnlockResponse(
    boolean success,
    String message,
    Long logId,
    LocalDateTime eventTime
) {
}

