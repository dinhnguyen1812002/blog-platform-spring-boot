package com.Nguyen.blogplatform.Component;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketLogAppender extends AppenderBase<ILoggingEvent> {
    private SimpMessagingTemplate messagingTemplate;
    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketLogAppender.applicationContext = applicationContext;
    }

    @Override
    public void start() {
        if (applicationContext != null) {
            messagingTemplate = applicationContext.getBean(SimpMessagingTemplate.class);
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        if (messagingTemplate != null) {
            String message = iLoggingEvent.getFormattedMessage();
            try {
                messagingTemplate.convertAndSend("/topic/logs", message);
            } catch (Exception e) {
                System.err.println("Error sending log message to WebSocket: " + e.getMessage());
            }
        }
    }
}
