package com.example.frontend_chat.Controller.SearchController;

import com.example.frontend_chat.DTO.ContactResponse;
import com.example.frontend_chat.DTO.SimpleUserDTO;
import com.example.frontend_chat.utils.Validation;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AddUserItem {
    @FXML
    private Label userName;
    @FXML
    private Label userEmail;
    @FXML
    private ImageView profileImage;
    @FXML
    private Button addButton;
    private  SearchController searchController;
    public void setUserData(ContactResponse response, SearchController searchController) {
        this.searchController = searchController;
        SimpleUserDTO user = response.getContact();
        String displayedName = user.getFirstName() + " " + user.getLastName();
        userName.setText(displayedName);
        userEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");

        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()
                && Validation.isValidUrl(user.getProfilePicture())) {
            profileImage.setImage(new Image(user.getProfilePicture(), true));
        }

        switch (response.getStatus()) {
            case PENDING -> {
                addButton.setDisable(true);
                addButton.setText("Pending Request");
                addButton.setStyle("-fx-background-color: #595959; -fx-text-fill: white;");
            }
            case ACCEPTED -> {
                addButton.setDisable(true);
                addButton.setText("Accepted");
                addButton.setStyle("-fx-background-color: #64fa7d; -fx-text-fill: white;");
            }
            case BLOCKED -> {
                addButton.setDisable(true);
                addButton.setText("Blocked");
                addButton.setStyle("-fx-background-color: #f45050; -fx-text-fill: white;");
            }
            case NONE -> {
                addButton.setDisable(false);
                addButton.setText("Add Friend");
                addButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
            }
        }

    }

    @FXML
    public void addUser( ActionEvent actionEvent ) {
        String receiverEmail = userEmail.getText();
        if (receiverEmail == null || receiverEmail.isEmpty()) {
            searchController.getNotificationPopup().displayErrorNotification("Invalid Action", "⚠️ Please select a valid user.");
            return;
        }
        addButton.setDisable(true);
        Task<Void> sendRequestTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ContactResponse contactResponse = searchController.getSearchAPI().addContactRequest(userEmail.getText());
                return null;
            }
            @Override
            protected void succeeded() {
                addButton.setText("Pending");
                addButton.setStyle("-fx-background-color: #ff8080; -fx-text-fill: white;");
                addButton.getStyleClass().remove("button-success");
                searchController.getNotificationPopup().displayMessageNotification("Request Sent", "✅ Friend request sent successfully!");
            }

            @Override
            protected void failed() {
                addButton.setDisable(false);
                Throwable e = getException();
                searchController.getNotificationPopup().displayErrorNotification("Send Failed", "❌ " + e.getMessage());
                System.out.println(e.getMessage());
            }
        };

        new Thread(sendRequestTask).start();
    }
}
