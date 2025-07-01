package com.example.frontend_chat.Controller.MainPageController;

import com.example.frontend_chat.DTO.ContactResponse;
import com.example.frontend_chat.DTO.SimpleUserDTO;
import com.example.frontend_chat.utils.ShowDialogs;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

public class PendingRequestItem {
    @FXML
    public Label userName;
    @FXML
    public Label userEmail;
    @FXML
    public ImageView avatar;
    @FXML
    public Button acceptButton;
    @FXML
    public Button rejectButton;
    @FXML
    public HBox requestItem;
    @Getter
    private FriendList friendList;
    @Getter
    @Setter
    private ContactResponse contactResponse;

    public void setData( ContactResponse contactResponse, FriendList friendList ) {
        this.friendList = friendList;
        this.contactResponse = contactResponse;

        SimpleUserDTO user = contactResponse.getContact();
        userName.setText(user.getFirstName() + " " + user.getLastName());
        userEmail.setText(user.getEmail());
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            avatar.setImage(new Image(user.getProfilePicture()));
        }
        acceptButton.setOnAction(_ -> {
            try{
                var response = friendList.getChatAppApi().acceptContactRequest(contactResponse.getContact().getEmail());
                if(response.getStatusCode().is2xxSuccessful()){
                    System.out.println(response.getBody());
                    friendList.fetchPendingRequestsAsync();
                    friendList.fetchFriendListAsync();
                }else{
                    ShowDialogs.showErrorDialog( "Failed to process request. Try again.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        rejectButton.setOnAction(_ -> {
            try{
                var response = friendList.getChatAppApi().rejectgContactRequest(contactResponse.getContact().getEmail());
                if(response.getStatusCode().is2xxSuccessful()){
                    System.out.println(response.getBody());
                    friendList.fetchPendingRequestsAsync();
                    friendList.fetchFriendListAsync();
                }else{
                    ShowDialogs.showErrorDialog( "Failed to process request. Try again.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}