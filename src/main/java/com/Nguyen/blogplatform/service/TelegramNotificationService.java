package com.Nguyen.blogplatform.service;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class TelegramNotificationService {
    private final RestTemplate restTemplate = new RestTemplate();
//    @Value("${telegram.bot.token}")
    private final String botToken ="jhdfgjkhf";
//    @Value("${telegram.chat.id}")
    private final String botId ="jfkdfgdffgdfgsdfg";

    public void sendErrorNotification(String errorType, String errorMessage, String endpoint) {

        String formattedMessage = String.format(
                "*Error Type:* %s\n*Message:* %s\n*Endpoint:* %s",
                errorType,
                errorMessage,
                endpoint
        );


        String telegramUrl = String.format(
                "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=Markdown",
                botToken,
                botId,
                formattedMessage
        );

        restTemplate.getForObject(telegramUrl, String.class);
    }
}
