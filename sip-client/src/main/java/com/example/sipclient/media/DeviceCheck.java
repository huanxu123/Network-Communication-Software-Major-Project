package com.example.sipclient.media;

import javax.sound.sampled.*;

public class DeviceCheck {
    public static void main(String[] args) {
        System.out.println(">>> 正在检查 Java 识别到的音频设备...");

        // 1. 检查默认设备
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, null);
        try {
            Line line = AudioSystem.getLine(info);
            System.out.println("✅ 当前 Java 使用的默认麦克风信息: \n" + line.getLineInfo());
        } catch (Exception e) {
            System.out.println("❌ 无法获取默认设备: " + e.getMessage());
        }

        System.out.println("\n>>> 系统中所有可用的录音设备:");
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info mi : mixerInfos) {
            // 只列出能录音的设备
            Mixer mixer = AudioSystem.getMixer(mi);
            if (mixer.isLineSupported(info)) {
                System.out.println(" - " + mi.getName());
            }
        }
    }
}