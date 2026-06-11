package com.smartgate.model;

import java.time.LocalDateTime;

public class Alarm {
    private int id;
    private LocalDateTime alarmTime;
    private String apartmentNo;
    private String alarmType; // FIRE, GAS, FLOOD, MOTION, DOOR_WINDOW
    private String sourceLabel;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private boolean resolved;

    public Alarm() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getAlarmTime() { return alarmTime; }
    public void setAlarmTime(LocalDateTime alarmTime) { this.alarmTime = alarmTime; }

    public String getApartmentNo() { return apartmentNo; }
    public void setApartmentNo(String apartmentNo) { this.apartmentNo = apartmentNo; }

    public String getAlarmType() { return alarmType; }
    public void setAlarmType(String alarmType) { this.alarmType = alarmType; }

    public String getSourceLabel() { return sourceLabel; }
    public void setSourceLabel(String sourceLabel) { this.sourceLabel = sourceLabel; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
}
