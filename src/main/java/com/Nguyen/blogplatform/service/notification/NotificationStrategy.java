package com.Nguyen.blogplatform.service.notification;

import com.Nguyen.blogplatform.model.NotificationHistory;
import com.Nguyen.blogplatform.model.NotificationTemplate;

import java.util.Map;

public interface NotificationStrategy {

    String getChannel();

    NotificationHistory send(NotificationHistory notification, NotificationTemplate template, Map<String, Object> templateData);

    boolean isAvailable(String userId);
}
