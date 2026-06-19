package com.Nguyen.blogplatform.domain.apikey.infrastructure.security;



import com.Nguyen.blogplatform.domain.apikey.application.ApiKeyService;
import com.Nguyen.blogplatform.domain.auth.application.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKeyHeader = request.getHeader("X-API-KEY");
        String apiSecretHeader = request.getHeader("X-API-SECRET");

        if (apiKeyHeader != null && apiSecretHeader != null) {
            apiKeyService.validateApiKey(apiKeyHeader, apiSecretHeader).ifPresent(apiKey -> {
                UserDetailsImpl userDetails = UserDetailsImpl.build(apiKey.getUser());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }
}
