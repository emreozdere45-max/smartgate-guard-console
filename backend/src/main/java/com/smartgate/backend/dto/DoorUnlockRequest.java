package com.smartgate.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record DoorUnlockRequest(
    @Min(1) @Max(2) Integer relayNo,
    @Size(max = 40) String doorId
) {
    public int relayOrDefault() {
        return relayNo == null ? 1 : relayNo;
    }

    public String doorIdOrDefault() {
        return doorId == null || doorId.isBlank() ? "main" : doorId;
    }
}

