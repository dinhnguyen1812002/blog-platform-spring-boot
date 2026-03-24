package com.Nguyen.blogplatform.payload.request.notification;

import com.Nguyen.blogplatform.Enum.EDeliveryChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelPreferenceRequest {

    private EDeliveryChannel channel;
    private Boolean enabled;
    private String digestMode;
    private Integer quietHoursStart;
    private Integer quietHoursEnd;
    private String emailAddress;
    private String pushToken;
    private String deviceType;
}
