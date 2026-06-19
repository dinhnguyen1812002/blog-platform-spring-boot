package com.Nguyen.blogplatform.domain.notification.dto;



import com.Nguyen.blogplatform.shared.enums.EDeliveryChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    @NotBlank(message = "Template code is required")
    private String templateCode;

    private List<String> userIds;

    private List<EDeliveryChannel> channels;

    private Map<String, Object> templateData;

    private boolean bulk;
}
