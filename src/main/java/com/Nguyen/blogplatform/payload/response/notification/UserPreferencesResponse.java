package com.Nguyen.blogplatform.payload.response.notification;

import com.Nguyen.blogplatform.model.UserNotificationPreferences;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesResponse {

    private String userId;
    private List<UserNotificationPreferences> preferences;
}
