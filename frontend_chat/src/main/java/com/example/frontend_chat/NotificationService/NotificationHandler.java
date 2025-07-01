package com.example.frontend_chat.NotificationService;

import javafx.scene.media.AudioClip;
import java.net.URL;

public abstract class NotificationHandler {

    private static final String SOUND_SUCCESS_PATH = "/sounds/notifications.mp3";
    private static final String SOUND_ERROR_PATH = "/sounds/error.mp3";

    public abstract void displayFriendRequestNotification(String senderEmail);

    public abstract void displayFriendResponseNotification(String senderEmail, String status);

    public abstract void displayMessageNotification(String title, String message);

    public abstract void displayErrorNotification(String title, String message);

    protected void playSound(String soundPath) {
        try {
            URL soundUrl = getClass().getResource(soundPath);
            if (soundUrl == null) {
                System.err.println("⚠️ Sound file not found: " + soundPath);
                return;
            }
            AudioClip audioClip = new AudioClip(soundUrl.toExternalForm());
            audioClip.play();
        } catch (Exception e) {
            System.err.println("⚠️ Error playing sound: " + e.getMessage());
        }
    }

    protected void playSuccessSound() {
        playSound(SOUND_SUCCESS_PATH);
    }

    protected void playErrorSound() {
        playSound(SOUND_ERROR_PATH);
    }

}