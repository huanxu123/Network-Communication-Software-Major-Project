package com.example.admin.service;

import com.example.admin.entity.StatsSummary;
import com.example.admin.repository.CallRecordRepository;
import com.example.admin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CallRecordRepository callRecordRepository;

    // 以前这里依赖 InMemoryStore，现在不需要了，直接查数据库！

    public StatsSummary snapshot() {
        // 1. 从你的数据库查用户总数
        int total = (int) userRepository.count();

        // 2. 从你的数据库查在线人数
        // 这里的 .isOnline() 调用的就是你自己写的 User.java 里的方法
        int online = (int) userRepository.findAll().stream()
                .filter(user -> user.isOnline())
                .count();

        // 3. 从你的数据库查通话记录总数
        // (队友原本想查"进行中"的电话，但你的表结构里暂时没有状态字段，我们先查总数，保证不报错)
        int activeCalls = (int) callRecordRepository.count();

        // 4. 模拟一下今日消息数 (这个作为演示数据没事)
        long messagesToday = online * 12L;

        return new StatsSummary(total, online, activeCalls, messagesToday);
    }
}