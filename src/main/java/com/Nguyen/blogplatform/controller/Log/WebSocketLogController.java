package com.Nguyen.blogplatform.controller.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logger")
public class WebSocketLogController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketLogController.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public String sendLogMessage() {
        String message = "Test log message sent at " + System.currentTimeMillis();
        
        // Send message directly to WebSocket topic
        messagingTemplate.convertAndSend("/topic/logs", message);
        
        // Also log the message using SLF4J
        logger.info(message);
        logger.debug("Debug message");
        logger.warn("Warning message");
        logger.error("Error message");
        
        return "Log messages sent to WebSocket and logger";
    }
}