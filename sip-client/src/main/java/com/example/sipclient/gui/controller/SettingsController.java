package com.example.sipclient.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javax.sound.sampled.*;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * 设置界面控制器
 */
public class SettingsController {

    @FXML private CheckBox notificationCheckBox;
    @FXML private CheckBox soundCheckBox;
    @FXML private Slider volumeSlider;
    @FXML private Label volumeLabel;
    @FXML private ComboBox<String> audioDeviceComboBox;
    @FXML private CheckBox autoStartCheckBox;
    @FXML private CheckBox saveHistoryCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Preferences preferences;
    private Stage stage;
    private Runnable onSettingsChanged;
    private Runnable onAudioDeviceChanged;

    @FXML
    public void initialize() {
        preferences = Preferences.userNodeForPackage(SettingsController.class);
        
        // 检测并初始化音频设备
        loadAudioDevices();
        
        // 音频设备切换监听
        audioDeviceComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                switchAudioDevice(newVal);
            }
        });
        
        // 音量滑块监听
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            volumeLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
        });
        
        // 加载保存的设置
        loadSettings();
    }

    /**
     * 加载保存的设置
     */
    private void loadSettings() {
        notificationCheckBox.setSelected(preferences.getBoolean("notification", true));
        soundCheckBox.setSelected(preferences.getBoolean("sound", true));
        volumeSlider.setValue(preferences.getDouble("volume", 80.0));
        volumeLabel.setText(String.format("%.0f%%", volumeSlider.getValue()));
        audioDeviceComboBox.setValue(preferences.get("audioDevice", "默认设备"));
        autoStartCheckBox.setSelected(preferences.getBoolean("autoStart", false));
        saveHistoryCheckBox.setSelected(preferences.getBoolean("saveHistory", true));
    }

    /**
     * 保存设置
     */
    @FXML
    private void handleSave() {
        preferences.putBoolean("notification", notificationCheckBox.isSelected());
        preferences.putBoolean("sound", soundCheckBox.isSelected());
        preferences.putDouble("volume", volumeSlider.getValue());
        preferences.put("audioDevice", audioDeviceComboBox.getValue());
        preferences.putBoolean("autoStart", autoStartCheckBox.isSelected());
        preferences.putBoolean("saveHistory", saveHistoryCheckBox.isSelected());
        
        // 触发设置变更回调
        if (onSettingsChanged != null) {
            onSettingsChanged.run();
        }
        
        showAlert("保存成功", "设置已保存", Alert.AlertType.INFORMATION);
        closeWindow();
    }

    /**
     * 取消设置
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }
    
    /**
     * 加载系统音频设备
     */
    private void loadAudioDevices() {
        List<String> devices = getAvailableAudioDevices();
        audioDeviceComboBox.getItems().clear();
        
        if (devices.isEmpty()) {
            audioDeviceComboBox.getItems().add("默认设备");
        } else {
            audioDeviceComboBox.getItems().addAll(devices);
        }
    }
    
    /**
     * 获取可用的音频设备列表
     */
    private List<String> getAvailableAudioDevices() {
        List<String> devices = new ArrayList<>();
        
        try {
            // 获取所有混音器（音频设备）
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            
            for (Mixer.Info info : mixerInfos) {
                Mixer mixer = AudioSystem.getMixer(info);
                
                // 检查是否支持目标数据线（输出设备）
                Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
                if (targetLineInfos.length > 0) {
                    String deviceName = info.getName();
                    if (!devices.contains(deviceName)) {
                        devices.add(deviceName);
                    }
                }
                
                // 检查是否支持源数据线（输入设备）
                Line.Info[] sourceLineInfos = mixer.getSourceLineInfo();
                if (sourceLineInfos.length > 0) {
                    String deviceName = info.getName();
                    if (!devices.contains(deviceName)) {
                        devices.add(deviceName);
                    }
                }
            }
            
            if (devices.isEmpty()) {
                devices.add("默认设备");
            }
            
            System.out.println("检测到 " + devices.size() + " 个音频设备");
            
        } catch (Exception e) {
            System.err.println("检测音频设备失败: " + e.getMessage());
            devices.add("默认设备");
        }
        
        return devices;
    }
    
    /**
     * 切换音频设备
     */
    private void switchAudioDevice(String deviceName) {
        System.out.println("切换音频设备: " + deviceName);
        
        // 触发音频设备切换回调
        if (onAudioDeviceChanged != null) {
            onAudioDeviceChanged.run();
        }
        
        // 这里可以添加实际的音频设备切换逻辑
        // 例如通知 MediaSession 或 AudioSession 切换设备
    }

    /**
     * 重置为默认设置
     */
    @FXML
    private void handleReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认重置");
        alert.setHeaderText("重置所有设置");
        alert.setContentText("确定要将所有设置恢复为默认值吗？");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                notificationCheckBox.setSelected(true);
                soundCheckBox.setSelected(true);
                volumeSlider.setValue(80.0);
                audioDeviceComboBox.setValue("默认设备");
                autoStartCheckBox.setSelected(false);
                saveHistoryCheckBox.setSelected(true);
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOnSettingsChanged(Runnable callback) {
        this.onSettingsChanged = callback;
    }
    
    public void setOnAudioDeviceChanged(Runnable callback) {
        this.onAudioDeviceChanged = callback;
    }

    /**
     * 获取当前音频设备
     */
    public static String getCurrentAudioDevice() {
        Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);
        return prefs.get("audioDevice", "默认设备");
    }

    /**
     * 获取音量设置
     */
    public static double getVolume() {
        Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);
        return prefs.getDouble("volume", 80.0);
    }

    /**
     * 是否启用通知
     */
    public static boolean isNotificationEnabled() {
        Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);
        return prefs.getBoolean("notification", true);
    }

    /**
     * 是否启用声音
     */
    public static boolean isSoundEnabled() {
        Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);
        return prefs.getBoolean("sound", true);
    }

    /**
     * 是否保存聊天记录
     */
    public static boolean isHistorySaveEnabled() {
        Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);
        return prefs.getBoolean("saveHistory", true);
    }

    private void closeWindow() {
        if (stage != null) {
            stage.close();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
