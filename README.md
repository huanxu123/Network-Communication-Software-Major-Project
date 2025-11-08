# SIP Communication 项目指南

本仓库实现一个基于 MSS + JAIN SIP 的多模块示例，涵盖即时消息、呼叫、群聊以及后台监控。按照 docs/guidance.md 中的阶段性目标，当前代码已经完成以下内容：

1. **阶段 1 – 最小 SIP 客户端**  
   - `sip-client` 模块提供 `SipUserAgent`，支持 REGISTER/UNREGISTER 与 SIP MESSAGE。  
   - `ui.ConsoleUI` 提供控制台操作：登录、发消息、查看历史。  
   - `chat` 目录封装 `MessageHandler`、`ChatSession`，用于记录对话。  

2. **阶段 2 – 呼叫与媒体骨架**  
   - `SipUserAgent.startCall()/hangup()` 实现 INVITE/ACK/BYE 流程，可发起/挂断点对点语音。  
   - `call.CallManager`/`CallSession` 结合 `media.AudioSession` 协调占位媒体会话，并在建立/结束呼叫时自动启停。  

3. **阶段 3 – 群聊/多方通话准备**  
   - `chat.GroupChatService` 管理群组、复用 MESSAGE 做群发。  
   - Console UI 支持创建群组与广播。  

4. **阶段 4 – 后台管理**  
   - `admin-server` 为 Spring Boot 应用，暴露 `/api/users`、`/api/calls`、`/api/stats` 等 REST 接口，并附带内存示例数据。  

5. **阶段 5 – 文档与协作**  
   - 本 README 概括开发步骤，`docs/guidance.md` 提供更细的 AI 协作规范。  

---

## 快速开始

### 1. 启动后台管理端
```bash
cd admin-server
mvn spring-boot:run
```
接口示例：
- `GET http://localhost:8081/api/users` – 查询示例用户及在线状态。
- `GET http://localhost:8081/api/calls` – 查看测试呼叫记录。
- `GET http://localhost:8081/api/stats` – 查看统计快照。

### 2. 启动 SIP 客户端
```bash
cd sip-client
mvn -q exec:java -Dexec.mainClass=com.example.sipclient.SipClientApplication
```
或直接执行 `java -cp target/sip-client-*.jar com.example.sipclient.SipClientApplication`（需先 `mvn package`）。控制台步骤：
1. 选择 “注册/登录”，输入 SIP URI（例：`sip:alice@example.com:5060`）、密码、本地 IP/端口。  
2. 使用菜单发送文本消息、查看历史或管理群聊。  
3. 收到 INVITE 时客户端会自动回铃并应答，同时在 “查看呼叫状态” 面板中可见。  

> 提示：当前媒体层为占位实现，仅打印日志，后续可换成 `javax.sound` 或联动 WebRTC 网关。

#### 阶段 1 操作流程
1. 完成上述注册后，选择菜单 `3` 输入目标 SIP URI 与文本，即可发送 MESSAGE。  
2. 当对端向本客户端发送文本时，`MessageHandler` 会立即在控制台打印 `收到来自 ...` 字样并写入会话记录；无需额外操作即可实时查看。  
3. 使用菜单 `4` 可再次查看最近 20 条对话内容，满足“接收并打印文本消息”的验收要求。  

#### 阶段 2 操作流程
1. 注册完成后，选择菜单 `5` 输入对端 SIP URI，客户端会立即发送 INVITE 并在控制台提示振铃状态。  
2. 被叫端（或模拟终端）应答后，客户端自动发送 ACK，并在控制台 `查看呼叫状态`（菜单 `7`）中显示为 `ACTIVE`；此时 `AudioSession` 会输出“音频会话启动”日志，表示占位媒体链路已建立。  
3. 若需要结束通话，选择菜单 `6` 输入同一 SIP URI，客户端将通过 BYE 挂断并停止媒体会话；同样地，若对端主动发送 BYE，控制台会收到提示并更新状态。  

### 3. 下一步建议
1. **真实消息与群聊**：对接 MSS 测试环境，补齐 MESSAGE 可靠性（重传、内容类型、群聊路由）。
2. **音频栈**：在 `media` 模块中接入 JMF、GStreamer 或 FFmpeg，使通话具备实际音频流。
3. **多方会议**：基于 `GroupChatService` 扩展呼叫控制，维护会议 ID、主持人角色与混音策略。
4. **后台持久化**：将 `admin-server` 内存数据迁移到数据库（MySQL/PostgreSQL），并通过 REST 与客户端互动。
5. **自动化测试**：为两个模块编写 JUnit/Mock 测试，集成 CI（如 GitHub Actions）保障构建质量。

更多细节请参考 `docs/guidance.md`，并在开发前确认每个阶段的验收标准。
