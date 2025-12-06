package com.example.admin.controller;

import com.example.admin.entity.CallRecord;
import com.example.admin.service.CallRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calls")
public class CallRecordController {

    @Autowired
    private CallRecordService callRecordService;

    // 保存通话: POST /api/calls/save
    @PostMapping("/save")
    public ResponseEntity<?> saveCall(@RequestBody Map<String, Object> params) {
        try {
            CallRecord record = callRecordService.saveRecord(
                    (String) params.get("caller"),
                    (String) params.get("callee"),
                    Long.valueOf(params.get("duration").toString()),
                    (String) params.get("type")
            );
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("保存失败: " + e.getMessage());
        }
    }

    // 查询历史: GET /api/calls/history?username=xxx
    @GetMapping("/history")
    public ResponseEntity<List<CallRecord>> getHistory(@RequestParam String username) {
        return ResponseEntity.ok(callRecordService.getUserHistory(username));
    }
}