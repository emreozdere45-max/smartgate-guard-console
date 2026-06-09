package com.smartgate.model;

import java.time.LocalDateTime;

public class GateLog {
    private int id;
    private LocalDateTime unlockTime;
    private String unlockMethod; // CONSOLE, CARD, PASSWORD
    private int gateId;
    private int residentId;

    public GateLog() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getUnlockTime() { return unlockTime; }
    public void setUnlockTime(LocalDateTime unlockTime) { this.unlockTime = unlockTime; }

    public String getUnlockMethod() { return unlockMethod; }
    public void setUnlockMethod(String unlockMethod) { this.unlockMethod = unlockMethod; }

    public int getGateId() { return gateId; }
    public void setGateId(int gateId) { this.gateId = gateId; }

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }
}