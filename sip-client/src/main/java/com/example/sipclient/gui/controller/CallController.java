package com.example.sipclient.gui.controller;

import com.example.sipclient.call.CallManager;
import com.example.sipclient.gui.model.Contact;
import com.example.sipclient.sip.SipUserAgent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 通话窗口控制器
 */
public class CallController {

    @FXML private Label contactNameLabel;
    @FXML private Label callStatusLabel;
    @FXML private Label timerLabel;
    @FXML private Button hangupButton;
    @FXML private Button muteButton;

    private Contact contact;
    private SipUserAgent userAgent;
    private CallManager callManager;
    private Timeline timer;
    private int seconds = 0;
    private boolean muted = false;
    private boolean isReceiver = false; // 是否为接听方

    public void setCallInfo(Contact contact, SipUserAgent userAgent, CallManager callManager) {
        setCallInfo(contact, userAgent, callManager, false);
    }

    public void setCallInfo(Contact contact, SipUserAgent userAgent, CallManager callManager, boolean isReceiver) {
        this.contact = contact;
        this.userAgent = userAgent;
        this.callManager = callManager;
        this.isReceiver = isReceiver;
        
        contactNameLabel.setText(contact.getDisplayName());
        
        if (isReceiver) {
            // 接听方立即显示通话中并开始计时
            callStatusLabel.setText("通话中");
            startTimer();
        } else {
            // 发起方先显示呼叫中，等待接通后再计时
            callStatusLabel.setText("呼叫中...");
            waitForCallEstablished();
        }
    }

    @FXML
    private void handleHangup() {
        try {
            userAgent.hangup(contact.getSipUri());
            stopTimer();
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMute() {
        muted = !muted;
        muteButton.setText(muted ? "取消静音" : "静音");
        // TODO: 实现静音功能
    }

    private void waitForCallEstablished() {
        // 创建一个轮询任务，检查呼叫是否已建立
        Timeline checkTimer = new Timeline(new KeyFrame(Duration.millis(500), event -> {
            if (callManager != null) {
                callManager.findByRemote(contact.getSipUri()).ifPresent(session -> {
                    if (session.getState() == com.example.sipclient.call.CallSession.State.ACTIVE) {
                        // 呼叫已建立，更新状态并开始计时
                        callStatusLabel.setText("通话中");
                        startTimer();
                    }
                });
            }
        }));
        checkTimer.setCycleCount(Timeline.INDEFINITE);
        checkTimer.play();
        
        // 设置最大等待时间（60秒）
        Timeline timeoutTimer = new Timeline(new KeyFrame(Duration.seconds(60), event -> {
            checkTimer.stop();
        }));
        timeoutTimer.play();
    }

    private void startTimer() {
        if (timer != null) {
            return; // 防止重复启动
        }
        
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            seconds++;
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int secs = seconds % 60;
            timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, secs));
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) hangupButton.getScene().getWindow();
        stage.close();
    }
}
