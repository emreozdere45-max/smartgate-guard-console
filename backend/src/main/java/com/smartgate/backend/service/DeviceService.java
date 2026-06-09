package com.smartgate.backend.service;

import com.smartgate.backend.dto.DeviceRequest;
import com.smartgate.backend.dto.DeviceResponse;
import com.smartgate.backend.entity.IntercomDevice;
import com.smartgate.backend.repository.IntercomDeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {
    private final IntercomDeviceRepository intercomDeviceRepository;

    public DeviceService(IntercomDeviceRepository intercomDeviceRepository) {
        this.intercomDeviceRepository = intercomDeviceRepository;
    }

    public List<DeviceResponse> findActive() {
        return intercomDeviceRepository.findByActiveTrueOrderByNameAsc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public DeviceResponse create(DeviceRequest request) {
        IntercomDevice device = new IntercomDevice();
        device.setName(request.name());
        device.setIpAddress(request.ipAddress());
        device.setCommandPort(request.portOrDefault());
        device.setLocation(request.location());
        device.setActive(request.activeOrDefault());
        return toResponse(intercomDeviceRepository.save(device));
    }

    public IntercomDevice findActiveById(Long id) {
        return intercomDeviceRepository.findById(id)
            .filter(device -> Boolean.TRUE.equals(device.getActive()))
            .orElseThrow(() -> new IllegalArgumentException("Active intercom device not found: " + id));
    }

    public IntercomDevice findDefaultActive() {
        return intercomDeviceRepository.findFirstByActiveTrueOrderByIdAsc()
            .orElseThrow(() -> new IllegalStateException("No active intercom device configured"));
    }

    private DeviceResponse toResponse(IntercomDevice device) {
        return new DeviceResponse(
            device.getId(),
            device.getName(),
            device.getIpAddress(),
            device.getCommandPort(),
            device.getLocation(),
            device.getActive()
        );
    }
}

