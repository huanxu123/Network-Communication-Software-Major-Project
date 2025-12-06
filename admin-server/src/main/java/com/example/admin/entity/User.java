package com.example.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // 关键指令：告诉数据库建一张叫 users 的表
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String nickname;

    private boolean isOnline;

    private LocalDateTime lastActiveTime;

    // --- Getter 和 Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
    public LocalDateTime getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(LocalDateTime lastActiveTime) { this.lastActiveTime = lastActiveTime; }
}