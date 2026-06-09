package com.smartgate.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record DoorUnlockRequest(
    Long deviceId,
    @Min(1) @Max(2) Integer relayNo,
    @Size(max = 40) String doorId
) {
    public static DoorUnlockRequest defaultsForDevice(Long deviceId) {
        return new DoorUnlockRequest(deviceId, 1, "main");
    }

    public DoorUnlockRequest withDeviceId(Long deviceId) {
        return new DoorUnlockRequest(deviceId, relayNo, doorId);
    }

    public int relayOrDefault() {
        return relayNo == null ? 1 : relayNo;
    }

    public String doorIdOrDefault() {
        return doorId == null || doorId.isBlank() ? "main" : doorId;
    }
}
