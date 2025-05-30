package com.example.chat_frontend.controller;

import com.example.chat_frontend.API.APIRequests;
import com.example.chat_frontend.Model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ProfileController {
    @FXML
    private ImageView profileImage;
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private TextField email;
    @FXML
    private TextField phoneNumber;
    @FXML
    private TextArea address;
    @FXML
    private TextField country;
    @FXML
    private ComboBox<String> gender;
    @FXML
    private Button saveButton;

    @FXML
    public void initialize() {
        gender.getItems().addAll("Male", "Female", "Prefer not to say");

        try {
            URL url = new URL("http://localhost:8080/api/profile");
            HttpURLConnection connection = APIRequests.GETHttpURLConnection(url);
            System.out.println("JSON Response: " + connection.toString());
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                System.out.println("JSON Response: " + response.toString()); // Debug: Log the response

                ObjectMapper mapper = new ObjectMapper();
                User user = mapper.readValue(response.toString(), User.class);

                // Populate fields with null checks
                firstName.setText(user.getFirstName() != null ? user.getFirstName() : "");
                lastName.setText(user.getLastName() != null ? user.getLastName() : "");
                email.setText(user.getEmail() != null ? user.getEmail() : "");
                phoneNumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
                address.setText(user.getAddress() != null ? user.getAddress() : "");
                country.setText(user.getCountry() != null ? user.getCountry() : "");
                gender.setValue(user.getGender() != null ? user.getGender() : "Prefer not to say");

                if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                    profileImage.setImage(new Image(user.getProfilePicture(), true));
                }
            } else {
                System.out.println("Failed to load profile: HTTP " + responseCode);
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        System.out.println("Saving profile: " + "First Name: " + firstName.getText() + ", Last Name: " + lastName.getText() +
                ", Email: " + email.getText() + ", Phone: " + phoneNumber.getText() +
                ", Address: " + address.getText() + ", Country: " + country.getText() +
                ", Gender: " + gender.getValue() + ", Profile Picture: " + profileImage.getImage());
    }
}