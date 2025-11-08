package com.example.sipclient.ui;

import com.example.sipclient.call.CallManager;
import com.example.sipclient.chat.GroupChatService;
import com.example.sipclient.chat.MessageHandler;
import com.example.sipclient.config.SipConfig;
import com.example.sipclient.sip.SipUserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.SipException;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

/**
 * 极简控制台 UI，满足阶段 1～3 的交互演示：注册、发消息、群聊与查看呼叫。
 */
public class ConsoleUI implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConsoleUI.class);

    private final Scanner scanner = new Scanner(System.in);
    private final MessageHandler messageHandler = new MessageHandler(System.out::println);
    private final GroupChatService groupChatService = new GroupChatService();
    private final CallManager callManager = new CallManager();

    private SipUserAgent userAgent;
    private SipConfig currentConfig;

    @Override
    public void run() {
        boolean exit = false;
        while (!exit) {
            printMenu();
            String option = scanner.nextLine().trim();
            try {
                switch (option) {
                    case "1" -> register();
                    case "2" -> unregister();
                    case "3" -> sendMessage();
                    case "4" -> showChats();
                    case "5" -> startCall();
                    case "6" -> hangupCall();
                    case "7" -> listCalls();
                    case "8" -> createGroup();
                    case "9" -> groupBroadcast();
                    case "0" -> exit = true;
                    default -> System.out.println("未知选项");
                }
            } catch (Exception ex) {
                log.error("操作失败", ex);
            }
        }
        shutdown();
    }

    private void printMenu() {
        System.out.println("\n===== SIP 控制台 =====");
        System.out.println("1. 注册/登录");
        System.out.println("2. 注销");
        System.out.println("3. 发送文本消息");
        System.out.println("4. 查看会话记录");
        System.out.println("5. 发起语音呼叫");
        System.out.println("6. 挂断呼叫");
        System.out.println("7. 查看呼叫状态");
        System.out.println("8. 创建群组");
        System.out.println("9. 群发消息");
        System.out.println("0. 退出");
        System.out.print("请选择: ");
    }

    private void register() throws Exception {
        if (userAgent != null && userAgent.isRegistered()) {
            System.out.println("已经注册。若需重新登录，请先注销。");
            return;
        }
        System.out.print("SIP 地址 (如 sip:alice@example.com): ");
        String userAddress = scanner.nextLine().trim();
        System.out.print("密码: ");
        String password = scanner.nextLine().trim();
        System.out.print("本地 IP (可回车默认 127.0.0.1): ");
        String localIp = scanner.nextLine().trim();
        if (localIp.isBlank()) {
            localIp = "127.0.0.1";
        }
        System.out.print("本地端口 (默认 5070): ");
        String portInput = scanner.nextLine().trim();
        int localPort = portInput.isBlank() ? 5070 : Integer.parseInt(portInput);

        currentConfig = SipConfig.builder()
                .userAddress(userAddress)
                .password(password)
                .localIp(localIp)
                .localPort(localPort)
                .registerTimeout(Duration.ofSeconds(8))
                .build();

        userAgent = new SipUserAgent(
                currentConfig.getUserAddress(),
                currentConfig.getPassword(),
                currentConfig.getLocalIp(),
                currentConfig.getLocalPort());
        userAgent.setMessageHandler(messageHandler);
        userAgent.setCallManager(callManager);

        boolean ok = userAgent.register(currentConfig.getRegisterTimeout());
        System.out.println(ok ? "注册成功" : "注册失败，请检查日志");
    }

    private void unregister() throws SipException, InterruptedException {
        if (userAgent == null) {
            return;
        }
        boolean ok = userAgent.unregister(currentConfig.getRegisterTimeout());
        System.out.println(ok ? "注销成功" : "注销失败");
    }

    private void sendMessage() throws SipException {
        requireAgent();
        System.out.print("目标 SIP URI: ");
        String target = scanner.nextLine().trim();
        System.out.print("内容: ");
        String content = scanner.nextLine();
        userAgent.sendMessage(target, content);
        messageHandler.handleOutgoingMessage(target, content);
    }

    private void showChats() {
        messageHandler.listSessions().forEach(session -> {
            System.out.println("--- " + session.getSessionId() + " ---");
            session.dumpRecentMessages().forEach(System.out::println);
        });
    }

    private void createGroup() {
        System.out.print("群组 ID: ");
        String groupId = scanner.nextLine().trim();
        System.out.print("成员列表（用逗号分隔的 SIP URI）: ");
        String members = scanner.nextLine();
        List<String> memberUris = List.of(members.split(","));
        groupChatService.defineGroup(groupId, memberUris.stream().map(String::trim).filter(s -> !s.isBlank()).toList());
        System.out.println("群组已创建");
    }

    private void groupBroadcast() throws SipException {
        requireAgent();
        System.out.print("群组 ID: ");
        String groupId = scanner.nextLine().trim();
        System.out.print("内容: ");
        String text = scanner.nextLine();
        groupChatService.broadcastMessage(userAgent, groupId, text);
    }

    private void startCall() throws Exception {
        requireAgent();
        System.out.print("目标 SIP URI: ");
        String target = scanner.nextLine().trim();
        userAgent.startCall(target);
    }

    private void hangupCall() throws SipException {
        requireAgent();
        System.out.print("挂断对象 SIP URI: ");
        String target = scanner.nextLine().trim();
        userAgent.hangup(target);
    }

    private void listCalls() {
        callManager.listSessions().forEach(session ->
                System.out.printf("[%s] %s -> %s 状态 %s%n",
                        session.isIncoming() ? "来电" : "去电",
                        session.getId(),
                        session.getRemoteUri(),
                        session.getState()));
    }

    private void requireAgent() {
        if (userAgent == null) {
            throw new IllegalStateException("请先注册");
        }
    }

    private void shutdown() {
        if (userAgent != null) {
            userAgent.shutdown();
        }
    }
}
