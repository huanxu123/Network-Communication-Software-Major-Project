# AI 编程指导文档（基于 MSS + JAIN SIP 的即时通信/通话系统）

> 本文用于指导 AI 编程助手（如 Cursor / ChatGPT 等）在本项目中协助开发。  
> 使用 AI 写代码/重构/查问题时，请尽量遵守本文的约定。

---

## 1. 项目简介

### 1.1 项目目标

本项目实现一个**类似 Linphone** 的基于 **SIP 协议** 的即时通信与音视频通话软件，整体结构为：

- **SIP 信令服务器**：MSS（Mobicents SIP Server，以下简称 MSS）
- **客户端**：基于 Java 的 PC 端应用（后期可扩展到移动端）
- **后台管理**：Java Web（如 Spring Boot）实现的管理与统计系统

### 1.2 功能范围（课程要求对齐）

客户端主要功能：

1. **用户注册与登录**
   - 通过 MSS 完成 SIP 注册（REGISTER）
   - 支持基本账号管理（注册、修改、注销）

2. **即时消息**
   - 文本消息（SIP MESSAGE）
   - 预留图片/语音/视频消息接口（可以先用占位实现）

3. **音视频通话**
   - 点对点语音通话（SIP INVITE/200/ACK/BYE 流程）
   - 点对点视频通话（初期可以先模拟或仅实现信令）

4. **群聊 / 多方通话**
   - 支持多人文本群聊
   - 多方音视频通话可简化：先实现信令流程或设计方案即可

5. **后台管理与统计**
   - 用户信息管理
   - 在线状态查看
   - 通话记录、消息记录查询
   - 简单统计报表（用户数、在线人数、通话次数等）

---

## 2. 系统架构与技术栈

### 2.1 总体架构

推荐架构（逻辑上分三层）：

- **SIP 层**
  - 服务端：MSS（Mobicents SIP Server）
  - 客户端：Java 应用使用 **JAIN SIP** 作为客户端 SIP 协议栈

- **业务逻辑层**
  - 会话管理（呼叫状态机）
  - 用户、联系人、聊天会话管理
  - 媒体通道控制（音视频模块的抽象）

- **展示层**
  - PC 客户端 UI（可用 JavaFX / Swing，或先用控制台交互）
  - 管理后台 Web UI（如 Spring Boot + Thymeleaf / Vue 等）

### 2.2 技术选型

- 编程语言：**Java**
- SIP 客户端协议栈：**JAIN SIP**
- SIP 服务器：**MSS（Mobicents SIP Server）**
- 客户端 UI：
  - 最小可行版本：控制台（CLI）
  - 完整版本：JavaFX（优先）或 Swing
- 管理后台：
  - Spring Boot + REST API + 简单前端页面
- 构建工具：Maven 或 Gradle（二选一，默认用 Maven）
- 单元测试：JUnit

---

## 3. 项目分阶段开发计划（给 AI 的开发顺序）

> 当我让 AI 编写或修改代码时，一般按照以下阶段推进。  
> AI 在回答时请优先确保当前阶段功能完整、可编译、可运行。

### 阶段 1：基础环境与最小 SIP 客户端

目标：能在 MSS 上完成 SIP 注册与注销，并能收发简单文本消息。

AI 需要完成：

1. Maven 项目初始化（多模块或单模块）
2. 引入 JAIN SIP 依赖与基础配置
3. 实现：
   - SIPUserAgent：负责注册、注销、维护 SIP 会话
   - 简单的命令行交互：
     - 登录（输入 SIP 账号/密码/服务器地址）
     - 发送文本消息给指定用户
     - 接收并打印文本消息

### 阶段 2：音视频通话信令 + 简单媒体处理

目标：实现呼叫发起、接听、挂断的完整 SIP 流程，并预留媒体接口。

AI 需要完成：

1. 呼叫控制模块：
   - CallSession / CallManager 类
   - INVITE / 180 Ringing / 200 OK / ACK / BYE 的处理逻辑
2. 媒体接口：
   - 定义 MediaSession / AudioSession 接口
   - 先实现“假媒体”（例如打印日志或播放提示音），后续再接真音频

### 阶段 3：群聊与多方通话（可以适当简化）

目标：群聊文本稳定；多方通话可实现原型或设计方案。

AI 可按以下优先顺序：

1. 文本群聊：
   - 群 ID、成员管理
   - 基于 SIP MESSAGE 的群消息发送与展示
2. 多方语音通话：
   - 简化处理：由某个“会议控制端”维护参与者列表与信令转发
   - 或先只完成信令流程设计 + 部分实现

### 阶段 4：后台管理与统计

目标：通过 Web 管理页面查看用户、通话记录、在线情况。

AI 需要：

1. 创建 Spring Boot 管理后台模块
2. 提供基础 REST API：
   - 用户列表、在线状态
   - 通话记录查询
   - 消息记录简单统计
3. 前端简单页面（可以是模板引擎或简单 HTML）

---

## 4. 项目目录结构约定（建议）

> 当我让 AI“创建类/模块”时，请尽量遵守如下结构。

```text
project-root/
  pom.xml                     # Maven 父模块，聚合并管理版本
  docs/
    ai-guidance.md              # 本文件
  sip-client/
    src/main/java/
      com/example/sipclient/
        sip/                    # JAIN SIP 相关封装
          SipConfig.java
          SipUserAgent.java
          SipListenerImpl.java
        call/                   # 呼叫控制层
          CallSession.java
          CallManager.java
        media/                  # 媒体抽象层
          MediaSession.java
          AudioSession.java
        chat/                   # 文本/图片等消息
          ChatSession.java
          MessageHandler.java
        ui/                     # 客户端 UI
          ConsoleUI.java
          # 将来可以加 JavaFX UI
    src/test/java/...
  admin-server/
    src/main/java/
      com/example/admin/
        controller/
        service/
        repository/
        entity/
    src/test/java/...
  build-scripts/                # 部署、打包脚本（可选）
  README.md

### Maven 多模块说明

- 根 `pom.xml` 使用 `com.example.communication:project-parent` 作为父模块，统一声明 Java 版本、JAIN SIP 以及日志依赖。
- `sip-client` 模块聚焦于基于 JAIN SIP 的信令处理，默认引入 `javax.sip:jain-sip-ri` 与 `slf4j-api` 以便输出调试日志。
- `admin-server` 模块与 `sip-client` 解耦，通过依赖 `sip-client` 暴露的公共 API 复用呼叫与会话管理能力，后续可叠加 Spring Boot 等 Web 依赖实现管理后台。
