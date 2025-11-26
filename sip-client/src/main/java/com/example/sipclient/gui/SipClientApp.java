package com.example.sipclient.gui;

import com.example.sipclient.gui.controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * SIP 客户端主应用
 */
public class SipClientApp extends Application {

    private static Scene currentScene;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        currentScene = new Scene(loader.load());
        
        primaryStage.setTitle("SIP 通讯客户端");
        primaryStage.setScene(currentScene);
        primaryStage.setResizable(false);
        
        // 添加窗口关闭事件处理
        primaryStage.setOnCloseRequest(event -> {
            cleanup();
        });
        
        primaryStage.show();
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        System.out.println("[SipClientApp] 正在清理资源...");
        try {
            // 获取LoginController并清理
            Object controller = currentScene.getUserData();
            if (controller instanceof LoginController) {
                ((LoginController) controller).cleanup();
            }
        } catch (Exception e) {
            System.err.println("清理资源失败: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() throws Exception {
        System.out.println("[SipClientApp] 应用正在停止...");
        cleanup();
        super.stop();
    }

    /**
     * 获取当前场景
     */
    public static Scene getCurrentScene() {
        return currentScene;
    }
    
    /**
     * 获取主窗口
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}