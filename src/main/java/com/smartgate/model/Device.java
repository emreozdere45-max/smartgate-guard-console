package com.smartgate.model;

public class Device {
    private Long id;
    private String name;
    private String ipAddress;
    private int commandPort;
    private String location;
    private boolean active;

    public Device() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public int getCommandPort() { return commandPort; }
    public void setCommandPort(int commandPort) { this.commandPort = commandPort; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}