package com.example.admin.service;

import com.example.admin.entity.CallRecord;
import com.example.admin.repository.CallRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CallRecordService {

    @Autowired
    private CallRecordRepository callRecordRepository;

    // 保存通话记录
    public CallRecord saveRecord(String caller, String callee, Long duration, String type) {
        CallRecord record = new CallRecord();
        record.setCaller(caller);
        record.setCallee(callee);
        record.setDuration(duration);
        record.setType(type);
        record.setStartTime(LocalDateTime.now());
        return callRecordRepository.save(record);
    }

    // 获取某个用户的通话历史
    public List<CallRecord> getUserHistory(String username) {
        return callRecordRepository.findByCallerOrCalleeOrderByStartTimeDesc(username, username);
    }

    // 获取所有通话记录（为了配合 DashboardSnapshotService）
    public List<CallRecord> listCallRecords() {
        return callRecordRepository.findAll();
    }
}