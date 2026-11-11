package com.Nguyen.blogplatform.infrastructure.config;



import com.Nguyen.blogplatform.domain.apikey.infrastructure.security.ApiKeyAuthenticationFilter;
import com.Nguyen.blogplatform.domain.auth.application.CustomOAuth2UserService;
import com.Nguyen.blogplatform.domain.auth.application.UserDetailsServiceImpl;
import com.Nguyen.blogplatform.domain.user.domain.model.Role;
import com.Nguyen.blogplatform.infrastructure.security.AuthEntryPointJwt;
import com.Nguyen.blogplatform.infrastructure.security.AuthTokenFilter;
import com.Nguyen.blogplatform.infrastructure.security.OAuth2AuthenticationFailureHandler;
import com.Nguyen.blogplatform.infrastructure.security.OAuth2AuthenticationSuccessHandler;
import com.Nguyen.blogplatform.shared.exception.CustomAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @org.springframework.beans.factory.annotation.Value("${frontend-url}")
    private String frontendUrl;

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthTokenFilter        jwtAuthFilter;
    private final AuthEntryPointJwt      unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final PasswordEncoder passwordEncoder;
    private final ApiKeyAuthenticationFilter apiKeyAuthFilter;

    public SecurityConfig(
            UserDetailsServiceImpl userDetailsService,
            AuthTokenFilter jwtAuthFilter,
            AuthEntryPointJwt unauthorizedHandler,
            CustomAccessDeniedHandler accessDeniedHandler,
            CustomOAuth2UserService customOAuth2UserService,
            @Lazy OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
            OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
            PasswordEncoder passwordEncoder,
            ApiKeyAuthenticationFilter apiKeyAuthFilter
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.unauthorizedHandler = unauthorizedHandler;
        this.accessDeniedHandler = accessDeniedHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.passwordEncoder = passwordEncoder;
        this.apiKeyAuthFilter = apiKeyAuthFilter;
    }

    // -------------------------------------------------------------------------
    // Auth infrastructure beans
    // -------------------------------------------------------------------------

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // -------------------------------------------------------------------------
    // Security filter chain
    // -------------------------------------------------------------------------

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.requireCsrfProtectionMatcher(request -> {
                    String method = request.getMethod();
                    if (java.util.Set.of("GET", "HEAD", "TRACE", "OPTIONS").contains(method)) return false;
                    boolean hasCookies = request.getCookies() != null && request.getCookies().length > 0;
                    String authorization = request.getHeader("Authorization");
                    boolean bearer = authorization != null && authorization.startsWith("Bearer ");
                    boolean apiKey = request.getHeader("X-API-KEY") != null && request.getHeader("X-API-SECRET") != null;
                    return hasCookies || (!bearer && !apiKey);
                }))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // IF_REQUIRED: OAuth2 redirect flow needs HttpSession for authorization request state.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth

                        // --- Infrastructure & docs ---
                        .requestMatchers(PUBLIC_INFRA).permitAll()

                        // --- Auth endpoints ---
                        .requestMatchers(PUBLIC_AUTH).permitAll()

                        // --- External API with API Key ---
                        .requestMatchers(PUBLIC_EXTERNAL).authenticated()

                        // --- Public GET: posts / search / categories / tags ---
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_CONTENT).permitAll()

                        // --- Public: user profiles ---
                        .requestMatchers(HttpMethod.GET, PUBLIC_USER_PROFILES).permitAll()

                        // --- Public: newsletter opt-in ---
                        .requestMatchers(PUBLIC_NEWSLETTER).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/series/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/series/search").permitAll()

                        // --- Operations: log viewer / log stream are admin-only ---
                        .requestMatchers("/logs/**", "/ws-logs/**").hasRole("ADMIN")

                        // --- Video: streaming is public, management is admin-only ---
                        .requestMatchers(HttpMethod.GET, "/video/**").permitAll()
                        .requestMatchers("/video/**").hasRole("ADMIN")

                        // --- Authenticated: general user actions ---
                        .requestMatchers(AUTHENTICATED_USER).authenticated()

                        // --- Role-based ---
                        .requestMatchers("/api/v1/user/**").hasRole("USER")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // --- Anything else requires login ---
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                );

        return http.build();
    }

    // -------------------------------------------------------------------------
    // CORS
    // -------------------------------------------------------------------------

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl.replaceAll("/+$", "")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // =========================================================================
    // URL constants — one place to maintain all route rules
    // =========================================================================

    private static final String[] PUBLIC_INFRA = {
            "/actuator/health",
            "/images/**",
            "/uploads/**",
            "/uploads/thumbnail/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/ws/**",
            "/traffic",
            "/profile/avatar/**"
    };

    private static final String[] PUBLIC_AUTH = {
            "/api/v1/auth/csrf",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            // Thymeleaf password pages (PasswordController)
            "/auth/forgot-password",
            "/auth/reset-password",
            "/oauth2/**",
            "/login/oauth2/**",
            "/api/v1/oauth/**"
    };

    private static final String[] PUBLIC_EXTERNAL = {
            "/api/v1/external/**"
    };

    private static final String[] PUBLIC_GET_CONTENT = {
            "/",
            "/api/v1/post",

            "/api/v1/post/featured",
            "/api/v1/post/search",
            "/api/v1/post/latest",
            "/api/v1/post/{slug}",
            "/api/v1/post/{id}/related",
            "/api/v1/post/categories/{slug}",
            "/api/v1/posts/{postId}/comments",   // read comments is public
            "/api/v1/comments/{commentId}/replies",
            "/api/v1/search",
            "/api/v1/categories/**",
            "/api/v1/tags/**",
            "/api/v1/traffic/**"
    };

    private static final String[] PUBLIC_USER_PROFILES = {
            "/api/v1/users/profile/{slug}",
            "/api/v1/users/top-authors",
            "/api/v1/users/public/{username}"
    };

    private static final String[] PUBLIC_NEWSLETTER = {
            "/api/v1/newsletter/subscribe",
            "/api/v1/newsletter/confirm/**",
            "/api/v1/newsletter/unsubscribe/**"
    };

    private static final String[] AUTHENTICATED_USER = {
            "/api/v1/auth/me",
            "/api/v1/auth/logout",
            "/api/v1/author/**",
            "/api/v1/user/profile",
            "/api/v1/auth/update-password",
            "/api/v1/profile",
            "/api/v1/profile/**",
            "/api/v1/posts/{postId}/comments",   // write comment requires auth
            "/api/v1/comments/**",
            "/api/v1/saved-posts/**",
            "/api/v1/post/{postId}/bookmark/**",
            "/api/v1/post/{postId}/featured",
            "/api/v1/posts/{postId}/report",     // report post requires auth
            "/api/v1/posts/reports/**",          // view own reports requires auth
            "/api/v1/newsletter/subscribers/**",
            "/api/v1/notifications/**",
            "/api/v1/api-keys/**"
    };

}
