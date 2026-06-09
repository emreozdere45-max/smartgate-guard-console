package com.smartgate.backend.controller;

import com.smartgate.backend.dto.DeviceRequest;
import com.smartgate.backend.dto.DeviceResponse;
import com.smartgate.backend.dto.DoorUnlockRequest;
import com.smartgate.backend.dto.DoorUnlockResponse;
import com.smartgate.backend.service.DeviceService;
import com.smartgate.backend.service.DoorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceService deviceService;
    private final DoorService doorService;

    public DeviceController(DeviceService deviceService, DoorService doorService) {
        this.deviceService = deviceService;
        this.doorService = doorService;
    }

    @GetMapping
    public List<DeviceResponse> list() {
        return deviceService.findActive();
    }

    @PostMapping
    public DeviceResponse create(@Valid @RequestBody DeviceRequest request) {
        return deviceService.create(request);
    }

    @PostMapping("/{deviceId}/door/unlock")
    public DoorUnlockResponse unlock(
        @PathVariable Long deviceId,
        @Valid @RequestBody(required = false) DoorUnlockRequest request
    ) {
        DoorUnlockRequest safeRequest = request == null ? DoorUnlockRequest.defaultsForDevice(deviceId) : request.withDeviceId(deviceId);
        return doorService.unlock(safeRequest);
    }
}

