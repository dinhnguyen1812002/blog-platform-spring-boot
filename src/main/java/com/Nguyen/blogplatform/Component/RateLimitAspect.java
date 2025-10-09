package com.Nguyen.blogplatform.Component;


import com.Nguyen.blogplatform.exception.RateLimitException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitAspect {
    public static  final String ERROR_MESSAGE = "To many request at endpoint %s from IP %s! Please try again after %d milliseconds!";
    private final ConcurrentHashMap<String, List<Long>> requestCounts = new ConcurrentHashMap<>();
    @Value("${app.rate.limit}")
    private int rateLimit;
    @Value("${app.rate.durationinms}")
    private long rateDuration;


    @Before("@annotation(com.Nguyen.blogplatform.validation.annotation.WithRateLimitProtection)")
    public void rateLimit(){
        final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        final String key = requestAttributes.getRequest().getRemoteAddr();
        final long currentTime = System.currentTimeMillis();
        requestCounts.putIfAbsent(key, new ArrayList<>());
        requestCounts.get(key).add(currentTime);
        cleanUpRequestCounts(currentTime);

        if (requestCounts.get(key).size() > rateLimit) {
            throw new RateLimitException(String.format(ERROR_MESSAGE, requestAttributes.getRequest().getRequestURI(), key, rateDuration));
        }

    }

    private void cleanUpRequestCounts(final long currentTime) {
        requestCounts.values().forEach(list -> {
            list.removeIf(time -> timeIsTooOld(currentTime, time));

        });
    }

    private boolean timeIsTooOld(long currentTime, Long timeToCheck) {
        return currentTime - timeToCheck > rateDuration;
    }


}
