package com.smartgate.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceRequest(
    @NotBlank @Size(max = 80) String name,
    @NotBlank @Size(max = 80) String ipAddress,
    @Min(1) @Max(65535) Integer commandPort,
    @Size(max = 120) String location,
    Boolean active
) {
    public int portOrDefault() {
        return commandPort == null ? 5432 : commandPort;
    }

    public boolean activeOrDefault() {
        return active == null || active;
    }
}

