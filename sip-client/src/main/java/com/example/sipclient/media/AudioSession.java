package com.example.sipclient.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sound.sampled.*;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 成员 A - AGC 增强版
 * 功能：Raw PCM 直通 + 噪声门 + 自动增益控制 (AGC)
 */
public class AudioSession implements MediaSession {

    private static final Logger log = LoggerFactory.getLogger(AudioSession.class);
    private volatile boolean running = false;
    private DatagramSocket socket;

    // 1. 静音开关 & 噪声门阈值
    private volatile boolean isMuted = false;
    private static final int NOISE_THRESHOLD = 500;

    // 2. [新增] AGC 参数
    private float currentGain = 1.0f; // 当前增益倍数 (默认1.0，即原样)
    private static final float MAX_GAIN = 5.0f; // 最大放大 5 倍
    private static final float TARGET_LEVEL = 20000.0f; // 目标音量 (short 最大是 32767，我们设 20000 比较舒适)
    private static final float GAIN_STEP = 0.05f; // 调整速度 (越小调整越慢，声音越平滑)

    private final AudioFormat format = new AudioFormat(8000, 16, 1, true, false); // Little Endian

    private final BlockingQueue<byte[]> jitterBuffer = new LinkedBlockingQueue<>();
    private String remoteIp;
    private int remotePort;

    @Override
    public void start() {}

    public void start(String targetIp, int targetPort, int localPort) {
        if (running) return;
        this.remoteIp = targetIp;
        this.remotePort = targetPort;
        this.running = true;
        this.isMuted = false;
        this.currentGain = 1.0f; // 重置增益

        try {
            socket = new DatagramSocket(localPort);
            log.info("音频启动: 噪声门 + AGC (自动增益)");

            new Thread(this::captureAndSend, "Audio-Sender").start();
            new Thread(this::receiveAndPlay, "Audio-Player").start();

        } catch (SocketException e) {
            log.error("启动失败", e);
            running = false;
        }
    }

    @Override
    public void stop() {
        if (!running) return;
        running = false;
        if (socket != null) socket.close();
        jitterBuffer.clear();
        log.info("停止");
    }

    public boolean isRunning() { return running; }

    public void setMute(boolean mute) { this.isMuted = mute; }

    // --- 发送端 ---
    private void captureAndSend() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(info);
            mic.open(format);
            mic.start();

            byte[] pcmBuffer = new byte[1024];
            InetAddress address = InetAddress.getByName(remoteIp);

            while (running) {
                int bytesRead = mic.read(pcmBuffer, 0, pcmBuffer.length);
                if (bytesRead > 0) {

                    // 1. 噪声门处理 (先去噪)
                    if (isMuted || isNoise(pcmBuffer, bytesRead)) {
                        Arrays.fill(pcmBuffer, (byte)0);
                        // 如果静音了，慢慢把增益降回 1.0，防止下次说话突然爆音
                        if (currentGain > 1.0f) currentGain -= GAIN_STEP;
                    } else {
                        // 2. [关键] AGC 处理 (再稳音量)
                        processAGC(pcmBuffer, bytesRead);
                    }

                    DatagramPacket packet = new DatagramPacket(pcmBuffer, bytesRead, address, remotePort);
                    socket.send(packet);
                }
            }
            mic.close();
        } catch (Exception e) {
            log.error("麦克风异常", e);
        }
    }

    // [新增] AGC 核心逻辑
    private void processAGC(byte[] buffer, int length) {
        // 第一步：找出这一段声音里的最大值 (峰值)
        int maxAmplitude = 0;
        for (int i = 0; i < length / 2; i++) {
            int low = buffer[2 * i] & 0xFF;
            int high = buffer[2 * i + 1];
            short sample = (short) ((high << 8) | low);
            int abs = Math.abs(sample);
            if (abs > maxAmplitude) maxAmplitude = abs;
        }

        // 第二步：计算我们需要多大的倍数才能达到目标音量
        // 如果当前最大是 500，目标是 20000，那理想倍数就是 40 倍
        // 但我们不能直接乘 40，要限制在 MAX_GAIN 以内
        float targetGain = 1.0f;
        if (maxAmplitude > 0) {
            targetGain = TARGET_LEVEL / maxAmplitude;
        }

        // 限制最大放大倍数
        if (targetGain > MAX_GAIN) targetGain = MAX_GAIN;
        // 限制最小倍数 (不能小于 0.1，否则声音就没了)
        if (targetGain < 0.1f) targetGain = 0.1f;

        // 第三步：平滑调整当前增益 (不能突变)
        if (currentGain < targetGain) {
            currentGain += GAIN_STEP; // 慢慢变大
        } else {
            currentGain -= GAIN_STEP; // 慢慢变小
        }

        // 第四步：应用增益到每一个样本
        for (int i = 0; i < length / 2; i++) {
            int low = buffer[2 * i] & 0xFF;
            int high = buffer[2 * i + 1];
            short sample = (short) ((high << 8) | low);

            // 乘上增益
            int amplified = (int) (sample * currentGain);

            // 防止爆音 (Clamp)：如果超过了 short 的范围，就强行按住
            if (amplified > 32767) amplified = 32767;
            if (amplified < -32768) amplified = -32768;

            // 写回 buffer
            short result = (short) amplified;
            buffer[2 * i] = (byte) (result & 0xFF);
            buffer[2 * i + 1] = (byte) ((result >> 8) & 0xFF);
        }
    }

    // 判断底噪
    private boolean isNoise(byte[] buffer, int length) {
        int maxAmplitude = 0;
        for (int i = 0; i < length / 2; i++) {
            int low = buffer[2 * i] & 0xFF;
            int high = buffer[2 * i + 1];
            short sample = (short) ((high << 8) | low);

            int abs = Math.abs(sample);
            if (abs > maxAmplitude) maxAmplitude = abs;
        }
        return maxAmplitude < NOISE_THRESHOLD;
    }

    // --- 接收与播放端 ---
    private void receiveAndPlay() {
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(format);
            speaker.start();

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (running) {
                try {
                    socket.receive(packet);
                    speaker.write(packet.getData(), 0, packet.getLength());
                } catch (SocketException e) {
                    break;
                }
            }
            speaker.close();
        } catch (Exception e) {
            log.error("播放错误", e);
        }
    }
}