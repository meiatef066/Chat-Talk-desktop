package com.example.chat_frontend.controller;

import com.example.chat_frontend.API.ApiClient;
import com.example.chat_frontend.API.ProfileApi;
import com.example.chat_frontend.DTO.UpdateUserProfileRequest;
import com.example.chat_frontend.Model.User;
import com.example.chat_frontend.utils.ShowDialogs;
import com.example.chat_frontend.utils.Validation;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ProfileController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_MIME_TYPES = {"image/png", "image/jpeg", "image/jpg"};

    @FXML private ImageView profileImage;
    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private TextField email;
    @FXML private TextField phoneNumber;
    @FXML private TextArea address;
    @FXML private TextField country;
    @FXML private ComboBox<String> gender;
    @FXML private Button saveButton;
    @FXML private Label errorLabel;

    private final ProfileApi profileApi = new ProfileApi(new ApiClient());
    private User user;
    private File selectedProfilePicture;

    @FXML
    public void initialize() {

        Task<User> loadProfileTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return profileApi.getProfile();
            }
        };

        loadProfileTask.setOnSucceeded(e -> {
            user = loadProfileTask.getValue();
            logger.info("Profile loaded for user: {}", user.getEmail());
            updateUI();
        });

        loadProfileTask.setOnFailed(e -> {
            logger.error("Error loading profile: {}", loadProfileTask.getException().getMessage());
            Platform.runLater(() -> ShowDialogs.showErrorDialog("Error loading profile: " + loadProfileTask.getException().getMessage()));
        });

        new Thread(loadProfileTask).start();
    }

    private void updateUI() {
        Platform.runLater(() -> {
            firstName.setText(user.getFirstName() != null ? user.getFirstName() : "");
            lastName.setText(user.getLastName() != null ? user.getLastName() : "");
            email.setText(user.getEmail() != null ? user.getEmail() : "");
            phoneNumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
            address.setText(user.getAddress() != null ? user.getAddress() : "");
            country.setText(user.getCountry() != null ? user.getCountry() : "");
            gender.setValue(user.getGender() != null ? user.getGender() : "Prefer not to say");
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                try {
                    profileImage.setImage(new Image(user.getProfilePicture(), true));
                } catch (Exception ex) {
                    logger.error("Failed to load profile picture: {}", ex.getMessage());
                    profileImage.setImage(null);
                }
            }
            validateAllFields();
        });
    }

    @FXML
    private void changeProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        selectedProfilePicture = fileChooser.showOpenDialog(null);
        if (selectedProfilePicture != null) {
            try {
                String mimeType = Files.probeContentType(selectedProfilePicture.toPath());
                if (mimeType == null || !isAllowedMimeType(mimeType)) {
                    ShowDialogs.showWarningDialog("Invalid image file type. Please select a PNG, JPG, or JPEG file.");
                    selectedProfilePicture = null;
                    return;
                }
                if (selectedProfilePicture.length() > MAX_FILE_SIZE) {
                    ShowDialogs.showWarningDialog("Image file size exceeds 5MB limit.");
                    selectedProfilePicture = null;
                    return;
                }
                profileImage.setImage(new Image(selectedProfilePicture.toURI().toString()));
            } catch (IOException e) {
                logger.error("Error validating image file: {}", e.getMessage());
                ShowDialogs.showErrorDialog("Error validating image file: " + e.getMessage());
                selectedProfilePicture = null;
            }
            validateAllFields();
        }
    }

//    @FXML
//    private void validateEmail() {
//        String emailText = email.getText().trim();
//        if (!Validation.isValidEmail(emailText)) {
//            email.setStyle("-fx-border-color: red;");
//            errorLabel.setText("Invalid email format");
//            saveButton.setDisable(true);
//        } else {
//            email.setStyle("-fx-border-color: #4a5568;");
//            errorLabel.setText("");
//            validateAllFields();
//        }
//    }



    private void validateAllFields() {
        boolean isValid = Validation.isValidEmail(email.getText().trim()) &&
                (phoneNumber.getText().trim().isEmpty() || Validation.isValidPhone(phoneNumber.getText().trim())) &&
                !firstName.getText().trim().isEmpty() &&
                !lastName.getText().trim().isEmpty();
        saveButton.setDisable(!isValid);
    }

    @FXML
    private void saveProfile() {
        validateAllFields();
        if (saveButton.isDisabled()) {
            ShowDialogs.showWarningDialog("Please correct all fields before saving.");
            return;
        }

        Task<User> saveProfileTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                UpdateUserProfileRequest request = new UpdateUserProfileRequest();
                request.setFirstName(firstName.getText().trim());
                request.setLastName(lastName.getText().trim());
                request.setEmail(email.getText().trim());
                request.setPhoneNumber(phoneNumber.getText().trim());
                request.setBio(address.getText().trim());
                request.setCountry(country.getText().trim());
                request.setGender(gender.getValue());
                request.setProfilePicture(selectedProfilePicture);

                return profileApi.updateProfile(request);
            }
        };

        saveProfileTask.setOnSucceeded(e -> {
            user = saveProfileTask.getValue();
            logger.info("Profile updated successfully for user: {}", user.getEmail());
            selectedProfilePicture = null;
            Platform.runLater(() -> {
                updateUI();
                ShowDialogs.showInfoDialog("Profile updated successfully!");
            });
        });

        saveProfileTask.setOnFailed(e -> {
            String message = saveProfileTask.getException().getMessage();
            if (saveProfileTask.getException() instanceof IllegalArgumentException) {
                message = "Invalid profile data: " + message;
            } else if (saveProfileTask.getException() instanceof ApiClient.UnauthorizedException) {
                message = "Authentication failed. Please log in again.";
            } else {
                message = "Error saving profile: " + message;
            }
            logger.error("Error saving profile: {}", saveProfileTask.getException().getMessage());
            String finalMessage = message;
            Platform.runLater(() -> ShowDialogs.showErrorDialog(finalMessage));
        });

        new Thread(saveProfileTask).start();
    }

    private boolean isAllowedMimeType(String mimeType) {
        for (String allowedType : ALLOWED_MIME_TYPES) {
            if (allowedType.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }
}