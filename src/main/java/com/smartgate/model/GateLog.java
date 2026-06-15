package com.smartgate.model;

import java.time.LocalDateTime;

public class GateLog {
    private int id;
    private LocalDateTime unlockTime;
    private String unlockMethod;
    private String doorId;
    private int residentId;
    private Long deviceId;
    private String note;

    public GateLog() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getUnlockTime() { return unlockTime; }
    public void setUnlockTime(LocalDateTime unlockTime) { this.unlockTime = unlockTime; }

    public String getUnlockMethod() { return unlockMethod; }
    public void setUnlockMethod(String unlockMethod) { this.unlockMethod = unlockMethod; }

    public String getDoorId() { return doorId; }
    public void setDoorId(String doorId) { this.doorId = doorId; }

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}