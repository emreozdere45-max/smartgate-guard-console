package com.smartgate.backend.service;

import com.smartgate.backend.dto.DoorUnlockRequest;
import com.smartgate.backend.dto.DoorUnlockResponse;
import com.smartgate.backend.entity.GateLog;
import com.smartgate.backend.entity.IntercomDevice;
import com.smartgate.backend.intercom.IntercomCommandClient;
import org.springframework.stereotype.Service;

@Service
public class DoorService {
    private final IntercomCommandClient intercomCommandClient;
    private final GateLogService gateLogService;
    private final DeviceService deviceService;

    public DoorService(IntercomCommandClient intercomCommandClient, GateLogService gateLogService, DeviceService deviceService) {
        this.intercomCommandClient = intercomCommandClient;
        this.gateLogService = gateLogService;
        this.deviceService = deviceService;
    }

    public DoorUnlockResponse unlock(DoorUnlockRequest request) {
        String doorId = request.doorIdOrDefault();
        int relayNo = request.relayOrDefault();
        IntercomDevice device = request.deviceId() == null
            ? deviceService.findDefaultActive()
            : deviceService.findActiveById(request.deviceId());

        try {
            intercomCommandClient.unlockDoor(device.getIpAddress(), device.getCommandPort(), relayNo);
            GateLog log = gateLogService.saveConsoleUnlock(doorId, device.getId(), "Backend unlock request sent to " + device.getName());
            return new DoorUnlockResponse(true, "Door unlock command sent to " + device.getName(), log.getId(), log.getEventTime());
        } catch (Exception e) {
            GateLog log = gateLogService.saveConsoleUnlock(doorId, device.getId(), "Unlock failed for " + device.getName() + ": " + e.getMessage());
            return new DoorUnlockResponse(false, "Door unlock failed: " + e.getMessage(), log.getId(), log.getEventTime());
        }
    }
}
