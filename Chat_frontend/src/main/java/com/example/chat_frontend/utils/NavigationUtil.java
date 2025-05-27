package com.example.chat_frontend.utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;

public class NavigationUtil {

    /**
     * Switch scene by creating a NEW Scene each time
     * @param event the event that triggered navigation
     * @param fxmlPath path to FXML file (e.g. "/com/example/chat_frontend/view/Login.fxml")
     * @param windowTitle new window title
     */
    public static void switchSceneNewScene(ActionEvent event, String fxmlPath, String windowTitle) {
        try {
            Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(windowTitle);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Switch scene by REUSING the existing Scene and replacing root node
     * @param event the event that triggered navigation
     * @param fxmlPath path to FXML file
     * @param windowTitle new window title
     */
    public static void switchSceneReuseScene(ActionEvent event, String fxmlPath, String windowTitle) {
        try {
            Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            if (scene == null) {
                // No scene exists, create new one
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                // Reuse existing scene, just replace root
                scene.setRoot(root);
            }

            stage.setTitle(windowTitle);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

// Inside NavigationUtil class:

    public static void switchSceneWithFade(ActionEvent event, String fxmlPath, String windowTitle) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene currentScene = stage.getScene();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentScene.getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                try {
                    Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));
                    root.setOpacity(0.0);  // set opacity before fade in
                    Scene newScene = new Scene(root);
                    stage.setScene(newScene);
                    stage.setTitle(windowTitle);

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            fadeOut.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
