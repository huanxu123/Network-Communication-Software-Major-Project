package com.example.admin.service;

import com.example.admin.entity.CallRecord;
import com.example.admin.entity.DashboardSnapshot;
import com.example.admin.entity.StatsSummary;
import com.example.admin.entity.User; // 🔴 改动1：引入你的 User 类
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DashboardSnapshotService {

    private final StatsService statsService;
    private final UserService userService;
    private final CallRecordService callRecordService;

    public DashboardSnapshotService(
            StatsService statsService,
            UserService userService,
            CallRecordService callRecordService
    ) {
        this.statsService = statsService;
        this.userService = userService;
        this.callRecordService = callRecordService;
    }

    public DashboardSnapshot capture() {
        StatsSummary stats = statsService.snapshot();

        // 🔴 改动2：这里改成 List<User>，完美匹配你的 UserService
        List<User> users = userService.listUsers();

        List<CallRecord> calls = callRecordService.listCallRecords();

        // ⚠️ 注意：如果这里报错，说明 DashboardSnapshot 这个图纸也要改（见下一步）
        return new DashboardSnapshot(stats, users, calls, Instant.now());
    }
}