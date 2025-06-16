package com.example.chat_frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class Group {
    public VBox sidebarNavigator;
    public TextField searchField;
    public Button createGroupButton;
    public ScrollPane groupListContainer;
    public ListView groupList;
    public Label groupNameLabel;
    public VBox groupDetails;
    public Label membersLabel;
    public VBox messageList;
    public TextArea messageInput;
    public Button sendMessageButton;

    @FXML
    public void filterGroups( KeyEvent keyEvent ) {
    }
@FXML
    public void showCreateGroupPane( ActionEvent actionEvent ) {
    }
@FXML
    public void showAddMembersPane( ActionEvent actionEvent ) {
    }
@FXML
    public void createGroup( ActionEvent actionEvent ) {
    }
@FXML
    public void hideCreateGroupPane( ActionEvent actionEvent ) {
    }
}
