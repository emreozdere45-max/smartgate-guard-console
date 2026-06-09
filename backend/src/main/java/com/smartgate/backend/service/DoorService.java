package com.smartgate.backend.service;

import com.smartgate.backend.dto.DoorUnlockRequest;
import com.smartgate.backend.dto.DoorUnlockResponse;
import com.smartgate.backend.entity.GateLog;
import com.smartgate.backend.intercom.IntercomCommandClient;
import org.springframework.stereotype.Service;

@Service
public class DoorService {
    private final IntercomCommandClient intercomCommandClient;
    private final GateLogService gateLogService;

    public DoorService(IntercomCommandClient intercomCommandClient, GateLogService gateLogService) {
        this.intercomCommandClient = intercomCommandClient;
        this.gateLogService = gateLogService;
    }

    public DoorUnlockResponse unlock(DoorUnlockRequest request) {
        String doorId = request.doorIdOrDefault();
        int relayNo = request.relayOrDefault();

        try {
            intercomCommandClient.unlockDoor(relayNo);
            GateLog log = gateLogService.saveConsoleUnlock(doorId, "Backend unlock request sent to intercom");
            return new DoorUnlockResponse(true, "Door unlock command sent", log.getId(), log.getEventTime());
        } catch (Exception e) {
            GateLog log = gateLogService.saveConsoleUnlock(doorId, "Unlock failed: " + e.getMessage());
            return new DoorUnlockResponse(false, "Door unlock failed: " + e.getMessage(), log.getId(), log.getEventTime());
        }
    }
}

