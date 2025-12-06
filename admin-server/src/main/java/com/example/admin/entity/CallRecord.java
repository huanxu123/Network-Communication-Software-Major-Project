package com.example.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_records") // 数据库里叫 call_records 表
public class CallRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String caller; // 谁打的

    @Column(nullable = false)
    private String callee; // 打给谁

    @Column(nullable = false)
    private LocalDateTime startTime; // 开始时间

    private Long duration; // 通话时长（秒）

    private String type; // AUDIO 或 VIDEO

    // --- Getter 和 Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCaller() { return caller; }
    public void setCaller(String caller) { this.caller = caller; }
    public String getCallee() { return callee; }
    public void setCallee(String callee) { this.callee = callee; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}