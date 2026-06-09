package com.smartgate.model;

import java.time.LocalDateTime;

public class Alarm {
    private int id;
    private LocalDateTime alarmTime;
    private String apartmentNo;
    private String alarmType; // PIR, FIRE, GAS
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

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
}