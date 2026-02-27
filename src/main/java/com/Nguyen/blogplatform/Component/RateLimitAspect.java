package com.Nguyen.blogplatform.Component;


import com.Nguyen.blogplatform.exception.RateLimitException;
import glide.api.GlideClient;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.ExecutionException;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    public static final String ERROR_MESSAGE =
            "Too many requests at endpoint %s from IP %s! Please try again after %d milliseconds!";

    private final GlideClient glideClient;

    @Value("${app.rate.limit}")
    private int rateLimit;

    @Value("${app.rate.durationinms}")
    private long rateDuration;

    @Before("@annotation(com.Nguyen.blogplatform.validation.annotation.WithRateLimitProtection)")
    public void rateLimit() {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return;
        }

        String ip = attributes.getRequest().getRemoteAddr();
        String uri = attributes.getRequest().getRequestURI();

        String key = buildKey(uri, ip);

        try {

            Long count = glideClient.incr(key).join();

            if (count == 1) {
                glideClient.pexpire(key, rateDuration).get();
            }

            if (count > rateLimit) {
                throw new RateLimitException(
                        String.format(ERROR_MESSAGE, uri, ip, rateDuration)
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Rate limit interrupted", e);

        } catch (ExecutionException e) {
            throw new RuntimeException("Valkey execution error", e);
        }
    }

    private String buildKey(String uri, String ip) {
        return "ratelimit:" + uri + ":" + ip;
    }
}
