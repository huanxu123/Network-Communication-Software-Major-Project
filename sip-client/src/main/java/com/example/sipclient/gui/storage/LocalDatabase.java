package com.example.sipclient.gui.storage;

import com.example.sipclient.gui.model.Contact;
import com.example.sipclient.gui.model.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地数据库管理器 (SQLite)
 */
public class LocalDatabase {

    private static final String DB_URL = "jdbc:sqlite:sip_client.db";
    private Connection connection;

    /**
     * 初始化数据库
     */
    public void initialize() {
        try {
            // 加载 SQLite JDBC 驱动
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            
            // 创建表
            createTables();
            
            System.out.println("本地数据库初始化成功");
        } catch (Exception e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建数据表
     */
    private void createTables() throws SQLException {
        String createContactsTable = """
            CREATE TABLE IF NOT EXISTS contacts (
                user_id TEXT PRIMARY KEY,
                sip_uri TEXT NOT NULL,
                display_name TEXT NOT NULL,
                last_message TEXT,
                last_message_time TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createMessagesTable = """
            CREATE TABLE IF NOT EXISTS messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                contact_user_id TEXT NOT NULL,
                content TEXT NOT NULL,
                is_from_me INTEGER NOT NULL,
                timestamp TEXT NOT NULL,
                FOREIGN KEY (contact_user_id) REFERENCES contacts(user_id)
            )
        """;
                                                            
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createContactsTable);
            stmt.execute(createMessagesTable);
        }
    }

    /**
     * 保存联系人
     */
    public void saveContact(Contact contact) {
        String sql = """
            INSERT OR REPLACE INTO contacts (user_id, sip_uri, display_name, last_message, last_message_time)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contact.getUserId());
            pstmt.setString(2, contact.getSipUri());
            pstmt.setString(3, contact.getDisplayName());
            pstmt.setString(4, contact.getLastMessage());
            pstmt.setString(5, contact.getLastMessageTime() != null ? 
                contact.getLastMessageTime().toString() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("保存联系人失败: " + e.getMessage());
        }
    }

    /**
     * 加载所有联系人
     */
    public List<Contact> loadContacts() {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT * FROM contacts ORDER BY last_message_time DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Contact contact = new Contact(
                    rs.getString("user_id"),
                    rs.getString("sip_uri"),
                    rs.getString("display_name")
                );
                contact.setLastMessage(rs.getString("last_message"));
                
                String timeStr = rs.getString("last_message_time");
                if (timeStr != null && !timeStr.isEmpty()) {
                    contact.setLastMessageTime(LocalDateTime.parse(timeStr));
                }
                
                contacts.add(contact);
            }
        } catch (SQLException e) {
            System.err.println("加载联系人失败: " + e.getMessage());
        }

        return contacts;
    }

    /**
     * 保存消息
     */
    public void saveMessage(String contactUserId, Message message) {
        String sql = """
            INSERT INTO messages (contact_user_id, content, is_from_me, timestamp)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contactUserId);
            pstmt.setString(2, message.getContent());
            pstmt.setInt(3, message.isFromMe() ? 1 : 0);
            pstmt.setString(4, message.getTimestamp().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("保存消息失败: " + e.getMessage());
        }
    }

    /**
     * 加载联系人的消息历史
     */
    public List<Message> loadMessages(String contactUserId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE contact_user_id = ? ORDER BY timestamp ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contactUserId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Message msg = new Message(
                        rs.getString("content"),
                        rs.getInt("is_from_me") == 1,
                        LocalDateTime.parse(rs.getString("timestamp"))
                    );
                    messages.add(msg);
                }
            }
        } catch (SQLException e) {
            System.err.println("加载消息失败: " + e.getMessage());
        }

        return messages;
    }

    /**
     * 删除联系人及其消息
     */
    public void deleteContact(String userId) {
        try {
            connection.setAutoCommit(false);
            
            // 删除消息
            String deleteMessages = "DELETE FROM messages WHERE contact_user_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteMessages)) {
                pstmt.setString(1, userId);
                pstmt.executeUpdate();
            }
            
            // 删除联系人
            String deleteContact = "DELETE FROM contacts WHERE user_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteContact)) {
                pstmt.setString(1, userId);
                pstmt.executeUpdate();
            }
            
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("删除联系人失败: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 清空所有数据
     */
    public void clearAll() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM messages");
            stmt.execute("DELETE FROM contacts");
            System.out.println("已清空所有数据");
        } catch (SQLException e) {
            System.err.println("清空数据失败: " + e.getMessage());
        }
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("数据库连接已关闭");
            }
        } catch (SQLException e) {
            System.err.println("关闭数据库失败: " + e.getMessage());
        }
    }
}
