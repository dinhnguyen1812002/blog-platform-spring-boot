package com.Nguyen.blogplatform.Utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UrlUtils {
    public String getBaseEnvLinkURL() {
        String baseEnvLinkURL=null;
        HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        baseEnvLinkURL = "http://" + currentRequest.getLocalName();
        if(currentRequest.getLocalPort() != 80) {
            baseEnvLinkURL += ":" + currentRequest.getLocalPort();
        }
        if(!StringUtils.isEmpty(currentRequest.getContextPath())) {
            baseEnvLinkURL += currentRequest.getContextPath();
        }
        return baseEnvLinkURL;
    }
}
