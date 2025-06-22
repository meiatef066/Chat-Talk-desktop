package com.example.backend_chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

//    public void sendContactNotification(String receiverUsername, ContactRequestDto dto) {
//        messagingTemplate.convertAndSendToUser(receiverUsername, "/queue/notifications", dto);
//    }
}
