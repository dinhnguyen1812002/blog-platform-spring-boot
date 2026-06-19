package com.Nguyen.blogplatform.domain.notification.application;




import com.Nguyen.blogplatform.domain.notification.domain.model.NotificationHistory;
import com.Nguyen.blogplatform.domain.notification.domain.model.NotificationTemplate;
import java.util.Map;

public interface NotificationStrategy {

    String getChannel();

    NotificationHistory send(NotificationHistory notification, NotificationTemplate template, Map<String, Object> templateData);

    boolean isAvailable(String userId);
}
