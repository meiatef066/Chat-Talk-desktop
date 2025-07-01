package com.example.frontend_chat.NotificationService;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class NotificationPopup extends NotificationHandler {
    private static final String FXML_PATH = "/com/example/frontend_chat/items/NotificationPopup.fxml";
    private Runnable onMessageClick;
    private static final List<Stage> activeNotifications = new ArrayList<>();

    @Override
    public void displayMessageNotification(String title, String message) {
        Platform.runLater(() -> {
            playSuccessSound();
            showPopup(title, message, "/images/icons/message.png", "MESSAGE", null, onMessageClick);
        });
    }

    @Override
    public void displayErrorNotification(String title, String message) {
        Platform.runLater(() -> {
            playErrorSound();
            showPopup(title, message, "/images/icons/error.png", "ERROR", null, null);
        });
    }

    @Override
    public void displayFriendRequestNotification(String sender) {
        Platform.runLater(() -> {
            playSuccessSound();
            showPopup("New Friend Request", sender + " sent you a request!", "/images/icons/notification.png", "FRIEND_REQUEST", sender, null);
        });
    }

    @Override
    public void displayFriendResponseNotification(String sender, String status) {
        Platform.runLater(() -> {
            playSuccessSound();
            showPopup("Request " + status, sender + " " + status.toLowerCase() + " your request", "/images/icons/success.png", "FRIEND_RESPONSE", sender, null);
        });
    }

    private void showPopup(String title, String message, String iconPath, String notificationType, String senderEmail, Runnable onMessageClick) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
            if (loader.getLocation() == null) {
                System.err.println("⚠️ FXML file not found at: " + FXML_PATH);
                return;
            }
            AnchorPane root = loader.load();
            NotificationPopupController controller = loader.getController();
            controller.setData(title, message, iconPath, notificationType, senderEmail, onMessageClick);

            Stage stage = new Stage();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setAlwaysOnTop(true);

            // Stack notifications in bottom-left corner
            synchronized (activeNotifications) {
                double x = Screen.getPrimary().getVisualBounds().getMaxX() - 350 - 10; // Right edge - width - 10px padding
                double y = Screen.getPrimary().getVisualBounds().getMaxY() - 100 - 10; // Bottom edge - height - 10px padding
                y -= activeNotifications.size() * (100 + 5); // Stack upward with 5px gap
                stage.setX(x);
                stage.setY(y);
                activeNotifications.add(stage);
            }

            root.setOpacity(0);
            stage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            PauseTransition pause = new PauseTransition(Duration.seconds(3));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                stage.close();
                synchronized (activeNotifications) {
                    activeNotifications.remove(stage);
                    for (int i = 0; i < activeNotifications.size(); i++) {
                        Stage s = activeNotifications.get(i);
                        double y = Screen.getPrimary().getVisualBounds().getMaxY() - 100 - 10 - i * (100 + 5);
                        s.setY(y);
                    }
                }
            });

            SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
            sequence.play();
        } catch (Exception e) {
            System.err.println("⚠️ Error displaying popup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}