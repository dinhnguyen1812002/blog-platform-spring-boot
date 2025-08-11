package com.Nguyen.blogplatform.Utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UrlUtils {
//    public String getBaseEnvLinkURL() {
//        String baseEnvLinkURL=null;
//        HttpServletRequest currentRequest =
//                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//        baseEnvLinkURL = "http://" + currentRequest.getLocalName();
//        if(currentRequest.getLocalPort() != 80) {
//            baseEnvLinkURL += ":" + currentRequest.getLocalPort();
//        }
//        if(!StringUtils.isEmpty(currentRequest.getContextPath())) {
//            baseEnvLinkURL += currentRequest.getContextPath();
//        }
//        return baseEnvLinkURL;
//    }

    public static String getBaseEnvLinkURL() {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        // Lấy scheme (http / https), server name (domain) và port
        String scheme = request.getScheme();      // http hoặc https
        String serverName = request.getServerName(); // ví dụ: example.com
        int serverPort = request.getServerPort(); // ví dụ: 8080

        // Nếu port là 80 (http) hoặc 443 (https) thì bỏ qua không thêm vào
        boolean isDefaultPort = (scheme.equals("http") && serverPort == 80) ||
                (scheme.equals("https") && serverPort == 443);

        String baseUrl = scheme + "://" + serverName + (isDefaultPort ? "" : ":" + serverPort)+ "/";

        return baseUrl; // ví dụ: http://localhost:8888 hoặc https://example.com
    }

}
