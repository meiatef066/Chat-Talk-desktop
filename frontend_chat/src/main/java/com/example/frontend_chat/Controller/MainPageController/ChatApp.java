package com.example.frontend_chat.Controller.MainPageController;

import com.example.frontend_chat.DTO.NotificationPayload;
import com.example.frontend_chat.NotificationService.NotificationPopup;
import com.example.frontend_chat.WebSocket.GeneralWebSocketClient;
import com.example.frontend_chat.utils.TokenManager;
import javafx.scene.layout.VBox;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import java.lang.reflect.Type;

public class ChatApp {
    public VBox chatWindow;
    public VBox rightPane;
    private GeneralWebSocketClient wsClient;
    private NotificationPopup notificationPopup;
    private FriendList friendList;
    public void setComponent( FriendList friendList){
        this.friendList = friendList;
    }
    public void initialize() {
        notificationPopup = new NotificationPopup();
         wsClient = new GeneralWebSocketClient(
                "ws://localhost:8080/ws",
                TokenManager.getInstance().getToken(),
                true,  // Use SockJS
                true   // Enable automatic reconnect
        );

// Subscribe to friend request
        wsClient.addSubscription("/user/queue/friend-request", new StompFrameHandler() {
            @Override
            public Type getPayloadType( StompHeaders headers) {
                return NotificationPayload.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("🔔 Friend Request: " + payload.toString());
                if (payload instanceof NotificationPayload notification) {

                    String msg = notification.getMessage(); // "tara sent you a friend request"

                    System.out.println("🔔 Friend Request from Notification: " + msg);

                    notificationPopup.displayMessageNotification(String.valueOf(notification.getType()), msg);

                    // Optional: Extract more details if needed

                    System.out.println("🔧 Full Data: " + notification.getData());
                } else {
                    System.out.println("⚠️ Unexpected payload type: " + payload);
                }
            }
        });

// Subscribe to messages
        wsClient.addSubscription("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }
            @Override
            public void handleFrame( StompHeaders headers, Object payload) {
                System.out.println("💬 Message: " + payload);
            }
        });
// Subscribe to response to request notification
        wsClient.addSubscription("/user/queue/friend-request-response", new StompFrameHandler() {
            @Override
            public Type getPayloadType( StompHeaders headers) {
                return NotificationPayload.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("🔔 Friend response: " + payload.toString());
                if (payload instanceof NotificationPayload notification) {

                    String msg = notification.getMessage(); // "tara sent you a friend request"

                    System.out.println("🔔 response from : " + msg);

                    notificationPopup.displayMessageNotification(String.valueOf(notification.getType()), msg);

                    // Optional: Extract more details if needed

                    System.out.println("🔧 Full Data: " + notification.getData());
                } else {
                    System.out.println("⚠️ Unexpected payload type: " + payload);
                }
            }
        });

        wsClient.connect();

    }

}
