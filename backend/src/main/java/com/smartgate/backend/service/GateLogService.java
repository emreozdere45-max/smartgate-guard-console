package com.smartgate.backend.service;

import com.smartgate.backend.dto.GateLogResponse;
import com.smartgate.backend.entity.GateLog;
import com.smartgate.backend.repository.GateLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GateLogService {
    private final GateLogRepository gateLogRepository;

    public GateLogService(GateLogRepository gateLogRepository) {
        this.gateLogRepository = gateLogRepository;
    }

    public List<GateLogResponse> findRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return gateLogRepository.findByOrderByEventTimeDesc(PageRequest.of(0, safeLimit))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public GateLog saveConsoleUnlock(String doorId, Long deviceId, String note) {
        GateLog gateLog = new GateLog();
        gateLog.setEventTime(java.time.LocalDateTime.now());
        gateLog.setMethod("CONSOLE");
        gateLog.setDoorId(doorId);
        gateLog.setDeviceId(deviceId);
        gateLog.setNote(note);
        return gateLogRepository.save(gateLog);
    }

    private GateLogResponse toResponse(GateLog gateLog) {
        return new GateLogResponse(
            gateLog.getId(),
            gateLog.getEventTime(),
            gateLog.getMethod(),
            gateLog.getDoorId(),
            gateLog.getDeviceId(),
            gateLog.getNote()
        );
    }
}
