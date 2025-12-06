package com.example.admin.repository;

import com.example.admin.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 这是一个高级查询：查找 user1 和 user2 之间的所有聊天记录（不管谁发给谁），并按时间排序
    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.timestamp ASC")
    List<Message> findChatHistory(String user1, String user2);

    // 查找发给某个人的所有未读消息
    List<Message> findByReceiverAndIsReadFalse(String receiver);
}