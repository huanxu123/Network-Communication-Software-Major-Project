package com.example.admin.entity;

import java.time.Instant;
import java.util.List;

public class DashboardSnapshot {
    private StatsSummary stats;
    private List<User> users; // 🔴 改动：这里接受 List<User>
    private List<CallRecord> calls;
    private Instant timestamp;

    // 构造方法
    public DashboardSnapshot(StatsSummary stats, List<User> users, List<CallRecord> calls, Instant timestamp) {
        this.stats = stats;
        this.users = users;
        this.calls = calls;
        this.timestamp = timestamp;
    }

    // Getter 和 Setter
    public StatsSummary getStats() { return stats; }
    public void setStats(StatsSummary stats) { this.stats = stats; }

    public List<User> getUsers() { return users; } // 🔴 改动：返回 User
    public void setUsers(List<User> users) { this.users = users; }

    public List<CallRecord> getCalls() { return calls; }
    public void setCalls(List<CallRecord> calls) { this.calls = calls; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}