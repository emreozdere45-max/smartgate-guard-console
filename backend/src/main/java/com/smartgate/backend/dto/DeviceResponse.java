package com.smartgate.backend.dto;

public record DeviceResponse(
    Long id,
    String name,
    String ipAddress,
    Integer commandPort,
    String location,
    Boolean active
) {
}

