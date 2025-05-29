package com.example.chat_frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;

public class ProfileController {
    @FXML private ImageView profileImage;
    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private TextField email;
    @FXML private TextField phoneNumber;
    @FXML private TextArea address;
    @FXML private TextField status;
    @FXML private ComboBox<String> gender;
    @FXML private Button saveButton;
    @FXML
    public void initialize() {
        // Initialize form with default or fetched user data
        gender.getSelectionModel().select("Prefer not to say");
        // Example: Fetch user data from backend and set fields
        // firstName.setText("John");
        // lastName.setText("Doe");
        // email.setText("john.doe@example.com");
        // phoneNumber.setText("+1234567890");
        // address.setText("123 Main St, City, Country");
        // status.setText("Available");
        // gender.getSelectionModel().select("Male");
    }

    @FXML
    private void changeProfilePicture(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            profileImage.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void validateEmail(KeyEvent event) {
        String emailText = email.getText();
        if (!emailText.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            email.setStyle("-fx-border-color: red;");
            saveButton.setDisable(true);
        } else {
            email.setStyle("-fx-border-color: #4a5568;");
            saveButton.setDisable(false);
        }
    }

    @FXML
    private void validatePhoneNumber(KeyEvent event) {
        String phoneText = phoneNumber.getText();
        if (!phoneText.matches("^\\+?\\d{10,15}$")) {
            phoneNumber.setStyle("-fx-border-color: red;");
            saveButton.setDisable(true);
        } else {
            phoneNumber.setStyle("-fx-border-color: #4a5568;");
            saveButton.setDisable(false);
        }
    }

    @FXML
    private void saveProfile(ActionEvent event) {
        // Save data to backend (e.g., API call)
        System.out.println("Saving profile: " +
                "First Name: " + firstName.getText() +
                ", Last Name: " + lastName.getText() +
                ", Email: " + email.getText() +
                ", Phone: " + phoneNumber.getText() +
//              ", Address: " + address.getText() +
                ", Status: " + status.getText() +
                ", profile picture"+profileImage.getImage()+
                ", Gender: " + gender.getValue());
        // Example: Send data to backend API
    }
}