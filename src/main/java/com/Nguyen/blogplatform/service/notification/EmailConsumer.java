package com.Nguyen.blogplatform.service.notification;

import com.Nguyen.blogplatform.payload.EmailEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import glide.api.GlideClient;
import glide.api.models.exceptions.GlideException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Consumer for the email stream. It reads from Valkey and sends the emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final GlideClient glideClient;
    private final EmailServices emailServices;
    private final ObjectMapper objectMapper;

    private static final String STREAM_KEY = "stream:emails";
    private static final String GROUP_NAME = "email-consumer-group";
    private String consumerName;

    @PostConstruct
    public void init() throws ExecutionException, InterruptedException {
        try {
            this.consumerName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            this.consumerName = "unknown-consumer";
        }

        // Create group if not exists. Errors if group already exists, which we catch.
        try {
            glideClient.customCommand(new String[]{"XGROUP", "CREATE", STREAM_KEY, GROUP_NAME, "0", "MKSTREAM"}).get();
            log.info("Consumer Group '{}' created for stream '{}'", GROUP_NAME, STREAM_KEY);
        } catch (Exception e) {
            log.debug("Consumer Group might already exist: {}", e.getMessage());
        }
    }

    /**
     * Poll Valkey Stream for new email messages.
     * We use a scheduled task to pull messages.
     */
    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    public void consumeEmails() {
        try {
            // XREADGROUP GROUP groupName consumerName COUNT 10 STREAMS streamKey >
            Object response = glideClient.customCommand(new String[]{
                "XREADGROUP", "GROUP", GROUP_NAME, consumerName, "COUNT", "10", "STREAMS", STREAM_KEY, ">"
            }).get();

            if (response == null) return;

            // Glide returns complex nested objects for customCommand. 
            // We need to parse the response manually according to Redis RESP protocol
            // Structure: [ [streamName, [ [id, [field, value, ...]], ... ]], ... ]
            if (response instanceof Object[] streams) {
                for (Object streamObj : streams) {
                    Object[] streamData = (Object[]) streamObj;
                    // streamData[0] is stream name
                    Object[] messages = (Object[]) streamData[1];
                    for (Object messageObj : messages) {
                        Object[] messageData = (Object[]) messageObj;
                        String messageId = (String) messageData[0];
                        Object[] fields = (Object[]) messageData[1];
                        
                        // fields: ["event", "json_data"]
                        for (int i = 0; i < fields.length; i += 2) {
                            if ("event".equals(fields[i])) {
                                String json = (String) fields[i+1];
                                processEmail(messageId, json);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error consuming email stream", e);
        }
    }

    private void processEmail(String messageId, String json) {
        try {
            EmailEvent event = objectMapper.readValue(json, EmailEvent.class);
            log.info("Processing email for {} (ID: {})", event.recipient(), messageId);
            
            // Send email
            emailServices.sendEmail(event);
            
            // ACK message: XACK streamKey groupName messageId
            glideClient.customCommand(new String[]{"XACK", STREAM_KEY, GROUP_NAME, messageId}).get();
            log.info("Email ACKed: ID {}", messageId);
            
        } catch (Exception e) {
            log.error("Failed to process email ID {}: {}", messageId, e.getMessage());
            // Message remains in PEL (Pending Entries List) for retry if we don't ACK
        }
    }
}
