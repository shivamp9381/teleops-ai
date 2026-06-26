package com.teleops.teleops_ai.device.dto;

/**
 * Device Health Score DTO
 */
public class DeviceHealthScore {

    private String deviceId;
    private String deviceName;
    private int score;          // 0-100
    private String grade;       // A, B, C, D, F
    private String status;      // EXCELLENT, GOOD, FAIR, POOR, CRITICAL
    private int recentAlarms;
    private int openTickets;
    private String scoreBreakdown;

    public DeviceHealthScore(String deviceId, String deviceName,
                             int score, String grade, String status,
                             int recentAlarms, int openTickets,
                             String scoreBreakdown) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.score = score;
        this.grade = grade;
        this.status = status;
        this.recentAlarms = recentAlarms;
        this.openTickets = openTickets;
        this.scoreBreakdown = scoreBreakdown;
    }

    // Getters
    public String getDeviceId()       { return deviceId; }
    public String getDeviceName()     { return deviceName; }
    public int getScore()             { return score; }
    public String getGrade()          { return grade; }
    public String getStatus()         { return status; }
    public int getRecentAlarms()      { return recentAlarms; }
    public int getOpenTickets()       { return openTickets; }
    public String getScoreBreakdown() { return scoreBreakdown; }
}