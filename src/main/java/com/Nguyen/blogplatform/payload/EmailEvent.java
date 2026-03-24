package com.Nguyen.blogplatform.payload;

import java.io.Serializable;
import java.util.Map;

/**
 * Event data to be put into Valkey Stream for email processing.
 */
public record EmailEvent(
    String type,
    String recipient,
    String subject,
    String template,
    Map<String, Object> data
) implements Serializable {
    public static final String TYPE_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String TYPE_NEWSLETTER_CONFIRM = "NEWSLETTER_CONFIRM";
    public static final String TYPE_NEWSLETTER_POSTS = "NEWSLETTER_POSTS";
}
