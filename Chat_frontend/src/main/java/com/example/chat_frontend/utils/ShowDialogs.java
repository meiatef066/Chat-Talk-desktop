package com.example.chat_frontend.utils;

import javafx.scene.control.Alert;

public class ShowDialogs {
  public static void showWarningDialog( String s ) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Action Required!");
        alert.setContentText(s);

        alert.showAndWait();
    }

    public static void showErrorDialog( String s ) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error ");
        alert.setHeaderText("Something went wrong!");
        alert.setContentText(s);

        alert.showAndWait();
    }
}
