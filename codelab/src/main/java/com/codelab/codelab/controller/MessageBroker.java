package com.codelab.codelab.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
@RestController
public class MessageBroker {

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/broadcast")
    public void broadcastMessage(@Payload String message) {
        log.info("message: " + message);
        messagingTemplate.convertAndSend("/queue/reply", "You have a message \n\r from someone: " + message);
    }
}
