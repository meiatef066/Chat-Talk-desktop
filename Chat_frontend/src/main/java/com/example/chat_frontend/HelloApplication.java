package com.example.chat_frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start( Stage stage ) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("ChatApp.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Chat & Talk 🦜");
        stage.setScene(scene);
        stage.show();
    }

    public static void main( String[] args ) {
        launch();
    }
}