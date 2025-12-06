package com.example.admin.controller;

import com.example.admin.entity.Message;
import com.example.admin.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // 发消息接口: POST http://localhost:8080/api/messages/send
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> params) {
        try {
            Message msg = messageService.sendMessage(
                    params.get("sender"),
                    params.get("receiver"),
                    params.get("content")
            );
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("发送失败: " + e.getMessage());
        }
    }

    // 查历史接口: GET http://localhost:8080/api/messages/history?user1=A&user2=B
    @GetMapping("/history")
    public ResponseEntity<List<Message>> getHistory(
            @RequestParam String user1,
            @RequestParam String user2) {
        return ResponseEntity.ok(messageService.getChatHistory(user1, user2));
    }
}