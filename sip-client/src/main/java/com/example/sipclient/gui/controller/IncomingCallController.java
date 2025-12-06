package com.example.sipclient.gui.controller;

import com.example.sipclient.sip.SipUserAgent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * 来电弹窗控制器
 */
public class IncomingCallController {

    @FXML private Label callerLabel;
    @FXML private Button acceptButton;
    @FXML private Button rejectButton;

    private String fromUri;
    private String sessionId;
    private SipUserAgent userAgent;

    public void setCallInfo(String fromUri, String sessionId, SipUserAgent userAgent) {
        this.fromUri = fromUri;
        this.sessionId = sessionId;
        this.userAgent = userAgent;
        
        callerLabel.setText(extractDisplayName(fromUri));
    }

    @FXML
    private void handleAccept() {
        try {
            userAgent.answerCall(fromUri);
            closeWindow();
            // 打开通话窗口
            openCallWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReject() {
        try {
            userAgent.rejectCall(fromUri);
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) acceptButton.getScene().getWindow();
        stage.close();
    }

    private String extractDisplayName(String uri) {
        if (uri.contains("@")) {
            String userId = uri.substring(uri.indexOf(":") + 1, uri.indexOf("@"));
            return "用户 " + userId;
        }
        return uri;
    }

    private void openCallWindow() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/call.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            
            CallController controller = loader.getController();
            // 创建联系人对象（参数顺序：userId, sipUri, displayName）
            com.example.sipclient.gui.model.Contact contact = new com.example.sipclient.gui.model.Contact(
                extractUserId(fromUri),
                fromUri,
                extractDisplayName(fromUri)
            );
            controller.setCallInfo(contact, userAgent, userAgent.getCallManager(), true); // true表示是接听方
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(scene);
            stage.setTitle("通话中 - " + extractDisplayName(fromUri));
            stage.setResizable(false);
            stage.show();
            
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private String extractUserId(String uri) {
        if (uri.contains("@")) {
            return uri.substring(uri.indexOf(":") + 1, uri.indexOf("@"));
        }
        return uri;
    }
}
