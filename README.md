# SIP 通信系统

> 基于 JAIN SIP + Spring Boot 的企业级即时通信系统  
> 采用 C/S 架构：Java 服务器 + Java PC 客户端

## 项目简介

本项目是一个完整的 SIP 通信系统，实现类似企业 IM 的即时通信功能。系统采用 **三层架构**：

- **SIP 服务器（MSS）** - 负责 SIP 协议处理和路由
- **Java 业务服务器（admin-server）** - 提供用户管理、消息存储、通话记录等业务功能
- **Java PC 客户端（sip-client）** - 桌面客户端，提供用户交互界面

## 系统架构

```
┌─────────────────┐         ┌─────────────────┐
│   Java PC 客户端   │ ←─SIP─→ │   MSS SIP 服务器  │
│  (sip-client)   │         │                 │
└────────┬────────┘         └─────────────────┘
         │
         │ HTTP REST API
         │
         ↓
┌─────────────────┐
│  Java 业务服务器   │
│  (admin-server) │
│  - 用户管理       │
│  - 消息存储       │
│  - 通话记录       │
│  - 统计报表       │
└─────────────────┘
```

## 技术栈

| 模块 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 构建工具 | Maven | 3.x | 多模块项目管理 |
| JDK | OpenJDK | 17 | 运行环境 |
| SIP 协议栈 | JAIN SIP | 1.3.0-91 | SIP 协议实现 |
| SIP 服务器 | MSS (Mobicents) | - | 可选，也可用其他 SIP 服务器 |
| 服务端框架 | Spring Boot | 3.2.5 | REST API 和业务逻辑 |
| 安全认证 | JWT | 0.12.3 | Token 认证 |
| 客户端 UI | JavaFX | 21.0.1 | 现代化桌面 GUI ✅ 已实现 |
| 本地存储 | SQLite JDBC | 3.45.0.0 | 聊天记录本地缓存 ✅ 已实现 |
| 日志框架 | SLF4J + Logback | 2.0.12 / 1.5.6 | 日志管理 |
| 测试框架 | JUnit 5 + Mockito | 5.10.2 / 5.11.0 | 单元测试 |

## 项目结构

```
Project/
├── pom.xml                           # 父 POM，管理依赖版本
│
├── sip-client/                       # Java PC 客户端模块
│   ├── src/main/java/
│   │   └── com/example/sipclient/
│   │       ├── sip/                  # SIP 协议层
│   │       │   └── SipUserAgent.java # SIP 用户代理
│   │       ├── call/                 # 呼叫管理
│   │       │   ├── CallManager.java
│   │       │   └── CallSession.java
│   │       ├── chat/                 # 消息处理
│   │       │   ├── MessageHandler.java
│   │       │   └── ChatSession.java
│   │       ├── media/                # 媒体会话
│   │       │   └── AudioSession.java
│   │       ├── config/               # 配置管理
│   │       │   └── SipConfig.java
│   │       ├── ui/                   # 用户界面
│   │       │   ├── ConsoleMain.java       # 命令行界面（已实现）
│   │       │   ├── QuickStartUser101.java # 快速启动脚本（用户 101）
│   │       │   └── QuickStartUser102.java # 快速启动脚本（用户 102）
│   │       ├── gui/                  # 图形界面 ✅ 已实现
│   │       │   ├── SipClientApp.java      # JavaFX 主应用
│   │       │   ├── controller/            # FXML 控制器
│   │       │   │   ├── LoginController.java
│   │       │   │   ├── MainController.java
│   │       │   │   ├── IncomingCallController.java
│   │       │   │   ├── CallController.java
│   │       │   │   └── SettingsController.java     # ✅ 设置界面
│   │       │   ├── model/                 # 数据模型
│   │       │   │   ├── Contact.java
│   │       │   │   └── Message.java
│   │       │   └── storage/               # ✅ 本地存储
│   │       │       └── LocalDatabase.java         # SQLite 数据库管理
│   │       └── api/                  # 与服务器通信
│   │           └── ServerApiClient.java
│   ├── src/main/resources/
│   │   ├── fxml/                     # JavaFX 界面文件
│   │   │   ├── login.fxml                 # 登录界面
│   │   │   ├── main.fxml                  # 主界面
│   │   │   ├── incoming_call.fxml         # 来电弹窗
│   │   │   ├── call.fxml                  # 通话窗口
│   │   │   └── settings.fxml              # ✅ 设置界面
│   │   └── css/                      # ✅ 主题样式
│   │       ├── light-theme.css            # 浅色主题
│   │       └── dark-theme.css             # 深色主题
│   └── pom.xml
│
├── admin-server/                     # Java 业务服务器模块
│   ├── src/main/java/
│   │   └── com/example/admin/
│   │       ├── controller/           # REST API 控制器
│   │       │   ├── AuthController.java     # 认证接口
│   │       │   ├── UserController.java     # 用户管理
│   │       │   ├── MessageController.java  # 消息管理
│   │       │   ├── CallController.java     # 呼叫管理
│   │       │   └── StatsController.java    # 统计报表
│   │       ├── service/              # 业务逻辑层
│   │       │   ├── SipService.java         # SIP 服务封装
│   │       │   └── UserService.java
│   │       ├── entity/               # 数据模型
│   │       ├── repository/           # 数据访问层
│   │       ├── config/               # 配置类
│   │       │   ├── WebConfig.java          # Web 配置
│   │       │   └── SecurityConfig.java     # 安全配置
│   │       ├── util/                 # 工具类
│   │       │   └── JwtUtil.java            # JWT 工具
│   │       └── dto/                  # 数据传输对象
│   ├── src/main/resources/
│   │   └── application.yml           # 应用配置
│   └── pom.xml
│
├── docs/
│   ├── guidance.md                   # 开发指导文档
│   └── architecture.md               # 系统架构说明
│
├── README.md                         # 本文件
├── QUICKSTART.md                     # 快速开始指南
├── start-gui.ps1                    # GUI 客户端启动脚本 ✅
├── start-sip-user101.ps1            # 命令行客户端启动脚本（用户 101）
├── start-sip-user102.ps1            # 命令行客户端启动脚本（用户 102）
└── run-sip-client.ps1               # 通用命令行启动脚本
```

## 核心功能

### ✅ 已完成
- [x] SIP 协议实现（REGISTER/MESSAGE/INVITE/BYE）
- [x] 摘要认证（401/407 Challenge）
- [x] 点对点消息收发
- [x] 音视频呼叫信令（INVITE/ACK/BYE）
- [x] 呼叫状态管理
- [x] 来电接听/拒接（Y/N 交互）
- [x] 命令行客户端界面
- [x] **JavaFX 图形界面客户端** ✨
  - [x] 登录界面
  - [x] 主界面（联系人列表 + 聊天窗口）
  - [x] 即时消息收发（类微信气泡）
  - [x] 来电弹窗（接听/拒接）
  - [x] 通话窗口（计时器）
- [x] Spring Boot REST API 服务器
- [x] JWT 认证
- [x] 用户管理接口
- [x] 消息发送接口
- [x] 呼叫控制接口

### 🔄 开发中
- [ ] 客户端与服务器 API 集成
- [ ] 消息和通话记录持久化
- [ ] 群聊功能
- [ ] 文件传输
- [ ] 音频采集与播放（Java Sound API）

### 📋 待开发
- [ ] 视频通话
- [ ] NAT 穿透（STUN/TURN）
- [ ] 用户在线状态同步
- [ ] 消息推送通知
- [ ] 动态添加联系人

---

## 快速开始

### 环境要求

1. **JDK 17**
   ```powershell
   java -version  # 确认输出 "17.x.x"
   ```

2. **Maven 3.x**
   ```powershell
   mvn -v
   ```

3. **MSS SIP 服务器**（可选）
   - 用于真实 SIP 注册测试
   - 或使用其他 SIP 服务器（如 Kamailio、Asterisk）

### 启动步骤

#### 方式 1：图形界面客户端（推荐）✨

```powershell
# 启动 JavaFX GUI 客户端
.\start-gui.ps1
```

**或使用 Maven：**
```powershell
cd sip-client
mvn javafx:run
```

**GUI 功能特性：**
- 🎨 现代化界面设计（类微信风格）
- 💬 即时消息收发（蓝色/灰色气泡）
- 📞 语音呼叫发起/接听/拒接
- 👥 联系人列表管理（添加/编辑/删除）✅ 新增
- 🔍 联系人和消息搜索 ✅ 新增
- 😀 表情符号支持 ✅ 新增
- 🌗 主题切换（浅色/深色）✅ 新增
- ⚙️ 设置界面（通知/音量/音频设备）✅ 新增
- 💾 本地聊天记录存储（SQLite）✅ 新增
- 🔔 来电弹窗提醒
- ⏱️ 通话计时器
- 📊 未读消息徽章

**默认配置：**
- SIP URI: `sip:101@10.29.133.174:5060`
- 密码: `101`
- 本地 IP: `10.29.133.174`
- 本地端口: `5061`

#### 方式 2：命令行客户端（快速测试）

**启动用户 101：**
```powershell
.\start-sip-user101.ps1
```

**启动用户 102（测试用）：**
```powershell
.\start-sip-user102.ps1
```

**可用命令：**
- `help` - 显示帮助
- `msg <sip:target@host> <内容>` - 发送消息
- `call <sip:target@host>` - 发起呼叫
- `Y / N` - 接听/拒接来电
- `hangup <sip:target@host>` - 挂断
- `unregister` - 注销
- `exit` - 退出

#### 方式 3：通用命令行（自定义配置）

#### 方式 3：通用命令行（自定义配置）

```powershell
# 启动 SIP 客户端（需要手动输入配置）
.\run-sip-client.ps1
```

按提示输入：
- SIP URI（例：`sip:alice@192.168.1.100:5060`）
- 密码
- 本地 IP 和端口

#### 方式 4：完整系统（客户端 + 服务器）

**1. 启动业务服务器**
```powershell
cd admin-server
mvn spring-boot:run
```

服务器运行在 `http://localhost:8081`

**2. 启动客户端**

**图形界面：**
```powershell
.\start-gui.ps1
```

**命令行界面：**
```powershell
.\start-sip-user101.ps1
```

**或使用脚本：**
```powershell
.\run-sip-client.ps1
```

**3. 测试 API**
```powershell
# 测试登录
curl -X POST http://localhost:8081/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "sipUri": "sip:alice@192.168.1.100:5060",
    "password": "secret",
    "localIp": "192.168.1.50",
    "localPort": 5070
  }'

# 查看用户列表
curl http://localhost:8081/api/users

# 查看统计数据
curl http://localhost:8081/api/stats
```

---

## JavaFX 图形界面详解 ✨

### 界面组件

#### 1. 登录界面
- 简洁的表单设计
- 预填充默认配置
- 异步连接和注册
- 进度指示器和状态提示

#### 2. 主界面

**布局结构：**
```
┌─────────────────────────────────────────────────────┐
│  SIP 客户端 - sip:101@10.29.133.174:5060   [最小化][关闭] │
├──────────┬──────────────────────────────────────────┤
│ 联系人列表 │  [联系人名称]              [📞 语音通话]  │
│          │  ────────────────────────────────────── │
│ 👤 102    │                                          │
│ 最后消息  │     💬 聊天消息区域                      │
│ [1] 12:30│     （类微信气泡，蓝色=发送，灰色=接收）   │
│          │                                          │
│ 👤 111    │                                          │
│ 最后消息  │                                          │
│ 13:45    │  ────────────────────────────────────── │
│          │  ┌────────────────────────────────────┐ │
│ 👤 103    │  │ 输入消息...                        │ │
│          │  │ (Shift+Enter 换行, Enter 发送)     │ │
│          │  └────────────────────────────────────┘ │
│          │                            [发送]        │
├──────────┴──────────────────────────────────────────┤
│  状态栏：已连接                                      │
└─────────────────────────────────────────────────────┘
```

**功能特性：**
- **联系人列表**
  - 显示联系人头像（占位符）
  - 最后一条消息预览
  - 消息时间戳
  - 未读消息红色徽章
  - ➕ 快速添加联系人 ✅ 新增
  - 🔍 实时搜索（昵称/ID/消息）✅ 新增
  - 右键菜单（编辑/删除）✅ 新增
  
- **聊天窗口**
  - 消息气泡样式（发送=蓝色，接收=灰色）
  - 自动滚动到最新消息
  - 消息时间显示
  - Enter 发送，Shift+Enter 换行
  - 😀 表情符号选择器 ✅ 新增
  - 🔍 消息历史搜索 ✅ 新增
  - 📎 文件附件（占位）
  
- **语音通话**
  - 点击按钮发起呼叫
  - 自动打开通话窗口
  
- **设置功能** ✅ 新增
  - 通知开关（桌面通知/提示音）
  - 主题切换（浅色/深色）
  - 音量调节
  - 音频设备选择
  - 开机自启动
  - 聊天记录保存
  
- **数据持久化** ✅ 新增
  - SQLite 本地数据库
  - 联系人自动保存
  - 聊天记录本地缓存
  - 启动时自动加载历史

#### 3. 来电弹窗
- 模态对话框
- 显示来电方信息
- 接听（绿色）/ 拒接（红色）按钮
- 阻塞式交互

#### 4. 通话窗口
- 独立窗口
- 紫色渐变背景
- 实时通话计时（HH:MM:SS）
- 挂断按钮（红色）
- 静音按钮（待实现）

#### 5. 设置界面 ✅ 新增
- 通知设置（启用桌面通知、提示音）
- 外观设置（主题切换：浅色/深色）
- 音频设置（音量滑块、设备选择）
- 其他设置（开机自启动、保存聊天记录）
- 重置按钮（恢复默认设置）
- 使用 Java Preferences API 持久化配置

### 测试步骤

**场景 1：GUI 客户端 ↔ 命令行客户端消息测试**

1. 启动 GUI 客户端（用户 101）
   ```powershell
   .\start-gui.ps1
   ```

2. 启动命令行客户端（用户 102）
   ```powershell
   .\start-sip-user102.ps1
   ```

3. 在 GUI 中选择联系人 "102"，输入消息并发送

4. 在 102 终端观察是否收到消息

5. 在 102 终端输入：`msg sip:101@10.29.133.174:5060 你好`

6. GUI 应显示来自 102 的消息

**场景 2：GUI 发起呼叫测试**

1. GUI 中选择联系人 "102"

2. 点击"📞 语音通话"按钮

3. 102 终端应显示来电提示

4. 在 102 终端输入 `Y` 接听

5. GUI 显示通话窗口和计时器

6. 点击挂断按钮结束通话

**场景 3：GUI 接听来电测试**

1. 在 102 终端输入：`call sip:101@10.29.133.174:5060`

2. GUI 弹出来电对话框

3. 点击"✓ 接听"或"✗ 拒接"

### 技术实现

- **框架**: JavaFX 21.0.1
- **布局**: FXML + Controller
- **样式**: 内联 CSS（-fx-* 属性）
- **架构**: MVC 模式
  - **Model**: Contact, Message
  - **View**: FXML 文件
  - **Controller**: LoginController, MainController 等

### 已集成的后端功能

| 功能 | 实现状态 | 调用方法 |
|------|---------|---------|
| SIP 注册 | ✅ | `userAgent.register()` |
| 发送消息 | ✅ | `userAgent.sendMessage()` |
| 接收消息 | ✅ | `MessageHandler.handleIncomingMessage()` |
| 发起呼叫 | ✅ | `userAgent.makeCall()` |
| 接听来电 | ✅ | `userAgent.answerCall()` |
| 拒接来电 | ✅ | `userAgent.rejectCall()` |
| 挂断通话 | ✅ | `userAgent.hangup()` |
| 来电通知 | ✅ | `CallManager.IncomingCallListener` |

### 待实现功能

- [ ] 注销登录按钮
- [ ] 动态添加联系人
- [ ] 静音功能（RTP 控制）
- [ ] 联系人编辑/删除
- [ ] 消息历史持久化
- [ ] 文件传输
- [ ] 群组聊天
- [ ] 视频通话
- [ ] 表情符号
- [ ] 头像上传/显示

---

## 开发指南

### 客户端开发

#### 当前状态
- ✅ 命令行界面（`ConsoleMain.java`）- 已完成
- ✅ 图形界面（JavaFX）- **已完成** ✨
  - ✅ 登录界面
  - ✅ 主界面（联系人 + 聊天）
  - ✅ 来电弹窗
  - ✅ 通话窗口

#### GUI 架构

**目录结构：**
```
sip-client/src/main/
├── java/com/example/sipclient/gui/
│   ├── SipClientApp.java              # 主应用入口
│   ├── controller/
│   │   ├── LoginController.java       # 登录逻辑
│   │   ├── MainController.java        # 主界面逻辑
│   │   ├── IncomingCallController.java# 来电处理
│   │   └── CallController.java        # 通话控制
│   └── model/
│       ├── Contact.java               # 联系人模型
│       └── Message.java               # 消息模型
└── resources/fxml/
    ├── login.fxml                     # 登录界面
    ├── main.fxml                      # 主界面
    ├── incoming_call.fxml             # 来电弹窗
    └── call.fxml                      # 通话窗口
```

**核心类说明：**

- **SipClientApp** - JavaFX 应用入口，主题管理
- **LoginController** - 处理登录表单、异步注册、跳转主界面
- **MainController** - 管理联系人列表、聊天窗口、消息收发、呼叫发起
  - ✅ 新增：联系人搜索、添加、编辑、删除
  - ✅ 新增：消息搜索、表情支持
  - ✅ 新增：数据库集成（SQLite）
- **IncomingCallController** - 处理来电接听/拒接
- **CallController** - 管理通话状态、计时器、挂断操作
- **SettingsController** ✅ 新增 - 设置界面管理、配置持久化
- **LocalDatabase** ✅ 新增 - SQLite 数据库操作（联系人、消息）

#### 扩展开发建议

**1. 用户注销功能**
```java
// 在 MainController 中添加菜单栏或按钮
@FXML
private void handleLogout() {
    userAgent.unregister();
    database.close();
    // 跳转回登录界面
}
```

**2. 文件传输（点击 📎 按钮）**
```java
// 在 MainController 中实现文件选择
@FXML
private void handleAttachFile() {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(stage);
    if (file != null) {
        // 通过 SIP MESSAGE 或 HTTP 发送文件
        sendFile(file);
    }
}
```

**3. 群聊功能**
```java
// 扩展 Contact 模型支持群组
public class GroupChat extends Contact {
    private List<String> members;
    // 实现群消息分发
}
```

**4. 托盘图标和通知**

#### GUI 开发参考

#### GUI 开发参考

**JavaFX 官方资源：**
- 官网：https://openjfx.io/
- 文档：https://openjfx.io/javadoc/21/
- 教程：https://docs.oracle.com/javafx/2/

**FXML 参考：**
- Scene Builder（可视化设计工具）：https://gluonhq.com/products/scene-builder/
- FXML 规范：https://docs.oracle.com/javase/8/javafx/api/javafx/fxml/doc-files/introduction_to_fxml.html

**CSS 样式参考：**
- JavaFX CSS 参考：https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html

#### 命令行客户端开发

客户端需要实现 `ServerApiClient` 类，调用服务器 REST API：

```java
// 登录
POST /api/auth/login
{
  "sipUri": "sip:alice@server",
  "password": "secret",
  "localIp": "192.168.1.50",
  "localPort": 5070
}

// 发送消息（通过服务器记录）
POST /api/messages
Authorization: Bearer {token}
{
  "to": "sip:bob@server",
  "content": "Hello"
}

// 获取消息历史
GET /api/messages/sessions/{sessionId}
Authorization: Bearer {token}

// 获取联系人列表
GET /api/users
Authorization: Bearer {token}
```

### 服务器开发

#### 当前功能
- ✅ JWT 认证
- ✅ SIP 服务封装
- ✅ REST API（用户、消息、呼叫）
- ✅ CORS 配置

#### 待实现功能
1. **数据持久化** - 集成 JPA + MySQL/H2
2. **用户注册** - 新用户注册接口
3. **消息推送** - 实时消息通知（WebSocket 或长轮询）
4. **群聊管理** - 群组创建、成员管理
5. **文件存储** - 文件上传下载接口

#### 数据库设计（建议）

```sql
-- 用户表
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,  -- SIP URI
    display_name VARCHAR(100),
    password VARCHAR(255),
    status VARCHAR(20),           -- ONLINE/OFFLINE/BUSY
    created_at TIMESTAMP
);

-- 消息表
CREATE TABLE messages (
    id VARCHAR(50) PRIMARY KEY,
    from_user VARCHAR(255),
    to_user VARCHAR(255),
    content TEXT,
    timestamp BIGINT,
    status VARCHAR(20)            -- SENT/DELIVERED/READ
);

-- 通话记录表
CREATE TABLE call_records (
    id VARCHAR(50) PRIMARY KEY,
    caller VARCHAR(255),
    callee VARCHAR(255),
    start_time BIGINT,
    end_time BIGINT,
    duration INT,
    status VARCHAR(20)            -- COMPLETED/MISSED/REJECTED
);
```

---

## 测试指南

### 单元测试
```powershell
# 运行所有测试
mvn clean test

# 运行特定模块测试
mvn -pl sip-client test
mvn -pl admin-server test
```

### 集成测试

详见 `QUICKSTART.md` 中的完整测试场景。

**快速测试：**
1. 启动两个客户端实例（不同端口）
2. 在客户端 A：`msg sip:bob@server 你好`
3. 在客户端 B：观察是否收到消息

---

## 常见问题

### Q: 如何实现客户端 GUI？
A: 推荐使用 JavaFX：
1. 在 `sip-client/pom.xml` 添加 JavaFX 依赖
2. 创建 `src/main/java/com/example/sipclient/ui/gui/` 包
3. 实现 `MainWindow.java`（主窗口）、`ChatPanel.java`（聊天面板）等
4. 参考 JavaFX 官方文档：https://openjfx.io/

### Q: 客户端如何保持长连接？
A: 两种方案：
1. **SIP 协议** - 通过 SIP MESSAGE 接收消息（已实现）
2. **WebSocket** - 连接服务器接收推送（需在服务器添加 WebSocket 支持）

### Q: 如何处理离线消息？
A: 
1. 客户端启动时调用 `GET /api/messages/sessions` 获取未读消息
2. 服务器存储离线消息到数据库
3. 用户上线后推送或拉取离线消息

### Q: 多个客户端如何同步状态？
A: 建议实现：
1. 服务器维护用户在线状态
2. 客户端定期心跳（`PUT /api/auth/status`）
3. 使用 WebSocket 推送状态变化

---

## 开发路线图

### 阶段 1：核心功能（当前）
- [x] SIP 协议实现
- [x] 服务器 REST API
- [x] 命令行客户端

### 阶段 2：GUI 客户端（已完成）✅
- [x] JavaFX 主窗口
- [x] 登录界面
- [x] 联系人列表
- [x] 聊天界面（类微信气泡）
- [x] 来电弹窗
- [x] 通话窗口（带计时器）
- [x] 设置界面 ✅ 新增
- [x] 联系人管理（添加/编辑/删除/搜索）✅ 新增
- [x] 消息搜索功能 ✅ 新增
- [x] 表情符号支持 ✅ 新增
- [x] 主题切换（浅色/深色）✅ 新增
- [x] 本地数据存储（SQLite）✅ 新增

### 阶段 3：数据持久化（部分完成）
- [x] SQLite 数据库集成 ✅ 客户端已完成
- [x] 消息本地存储 ✅ 客户端已完成
- [x] 联系人持久化 ✅ 客户端已完成
- [ ] 服务器端数据库（MySQL/PostgreSQL）
- [ ] 通话记录存储
- [ ] 离线消息推送

### 阶段 4：高级功能
- [ ] 音频采集与播放
- [ ] 群聊功能
- [ ] 文件传输
- [ ] 视频通话

---

## 文档索引

### 核心文档
- **`README.md`** - 完整项目文档（本文件）
- **`QUICKSTART.md`** - 快速测试指南（5 分钟上手）
- **`CHANGELOG.md`** - 版本更新历史

### 归档文档（已完成的阶段性文档）
- **`docs/archive/TASK-DISTRIBUTION.md`** - 四人团队任务分配（已完成）
- **`docs/archive/GUI_FEATURES_COMPLETION.md`** - GUI 功能完成报告
- **`docs/archive/GUI_TESTING_GUIDE.md`** - GUI 测试指南

> 💡 **提示：** 新用户建议先阅读 [QUICKSTART.md](QUICKSTART.md)，5 分钟快速上手！

---

## 开发规范

### 代码风格
- 遵循 Google Java Style Guide
- 使用 IDE 自动格式化（IntelliJ IDEA / Eclipse）
- 变量命名采用驼峰命名法
- 常量使用全大写下划线

### Git 工作流
```powershell
# 创建功能分支
git checkout -b feature/your-feature-name

# 提交代码
git add .
git commit -m "[模块] 简短描述"

# 合并到主分支
git checkout main
git merge feature/your-feature-name
```

### 提交信息规范
```
[模块] 简短描述

详细说明（可选）

示例：
[GUI] 添加设置界面
[SIP] 修复注册超时问题
[DB] 实现消息持久化
```

---

## 常见问题 (FAQ)

### Q: 如何更换 SIP 服务器地址？
A: 修改登录界面的 SIP URI，或在命令行启动脚本中更改 `serverHost` 参数。

### Q: 如何添加新联系人？
A: 
- **GUI**: 点击左上角 ➕ 按钮，输入用户 ID
- **CLI**: 直接发送消息到新的 SIP URI

### Q: 聊天记录保存在哪里？
A: SQLite 数据库文件 `sip_client.db`（项目根目录），可在设置中关闭保存功能。

### Q: 如何切换主题？
A: 点击 ⚙ 设置按钮 → 外观设置 → 选择"浅色"或"深色" → 保存。

### Q: 音频通话为什么没有声音？
A: 当前只实现了 SIP 信令，RTP 音频传输功能开发中（参考 CHANGELOG.md 进度）。

### Q: 如何在同一台电脑测试多个客户端？
A: 使用不同端口：
- GUI（101）: `.\start-gui.ps1`（自动使用 5070）
- CLI（102）: `.\start-sip-user102.ps1`（自动使用 5071）

### Q: 客户端如何保持长连接？
A: 通过 SIP REGISTER 定期刷新注册（默认 3600 秒），接收消息使用 SIP MESSAGE。

### Q: 如何处理离线消息？
A: 
- **当前**: 客户端离线时消息丢失
- **计划**: 服务器存储离线消息，上线后推送（成员 B 开发中）

---

## 团队协作

### Git 工作流
```powershell
# 客户端开发分支
git checkout -b feature/gui-client

# 服务器开发分支
git checkout -b feature/database-integration

# 合并到主分支
git checkout main
git merge feature/gui-client
```

### 代码规范
- 遵循 Java 代码规范（Google Java Style Guide）
- 提交前运行 `mvn clean test` 确保测试通过
- 提交信息格式：`[模块] 简短描述`

---

## 许可证

本项目仅用于课程学习，未设置开源许可证。

---

## 联系方式

项目仓库：https://github.com/huanxu123/sip  
问题反馈：通过 GitHub Issues 提交
