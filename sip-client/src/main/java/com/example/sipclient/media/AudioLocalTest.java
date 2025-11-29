package com.example.sipclient.media;

public class AudioLocalTest {
    public static void main(String[] args) throws InterruptedException {
        // 1. 创建音频会话
        AudioSession session = new AudioSession();

        System.out.println("=========================================");
        System.out.println(">>> 最终音质测试 (大端序 + 降噪 + 软音量) <<<");
        System.out.println("-----------------------------------------");
        System.out.println("1. 说话测试：请大声说话，检查是否还有爆音。");
        System.out.println("2. 静音测试：请闭嘴 5 秒，检查底噪是否消失（噪声门生效）。");
        System.out.println("3. 程序将运行 25 秒...");
        System.out.println("=========================================");

        // 2. 启动回环 (自己发给自己)
        // IP: 127.0.0.1, 端口: 55555
        session.start("127.0.0.1", 55555, 55555);

        // 3. 运行 25 秒
        Thread.sleep(25000);

        // 4. 结束
        session.stop();
        System.out.println(">>> 测试结束 <<<");
    }
}