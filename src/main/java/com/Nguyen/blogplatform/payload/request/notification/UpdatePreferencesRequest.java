package com.Nguyen.blogplatform.payload.request.notification;

import com.Nguyen.blogplatform.Enum.EDeliveryChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferencesRequest {

    private List<ChannelPreferenceRequest> preferences;
}
