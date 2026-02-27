package com.Nguyen.blogplatform.config;

import com.Nguyen.blogplatform.exception.CustomAccessDeniedHandler;
import com.Nguyen.blogplatform.security.AuthEntryPointJwt;
import com.Nguyen.blogplatform.security.AuthTokenFilter;
import com.Nguyen.blogplatform.service.UserDetailsServiceImpl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthTokenFilter jwtAuthFilter;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    public SecurityConfig(
        UserDetailsServiceImpl userDetailsService,
        AuthTokenFilter jwtAuthFilter,
        AuthEntryPointJwt unauthorizedHandler,
        CustomAccessDeniedHandler accessDeniedHandler
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.unauthorizedHandler = unauthorizedHandler;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(
            userDetailsService
        );

        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            )
            .authorizeHttpRequests(auth ->
                auth
                    // --- PUBLIC ENDPOINTS ---
                    .requestMatchers(
                        "/",
                        "/fuck",
                        "/actuator/**",
                        "/images/**",
                        "/uploads/**",
                        "/uploads/thumbnail/**",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/api-docs/**",
                        "/ws/**",
                        "/profile/avatar/**",
                        "/api/v1/users/profile/{slug}",
                        "/ws-logs/**",
                        "/video/**",
                        "/logs/**",
                         "/api/v1/auth/refresh-token",
                        "/logger/**",
                        "/traffic",
                        "/auth/**",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/api/v1/oauth/**"

                    )
                    .permitAll()
                    // --- AUTH & UTILITY ---
                    .requestMatchers(

                        "/api/v1/auth/logout",
                        "/api/v1/auth/**",
                        "/api/v1/jwt/decode",
                        "/api/v1/jwt/validate",
                        "/api/debug/**"
                    )
                    .permitAll()
                    // --- SERIES ---
                    .requestMatchers("/api/v1/series/**")
                    .permitAll()
                    // --- POSTS / TAGS / CATEGORIES (public GET) ---
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/post",
                        "/api/v1/post/featured",
                        "/api/v1/post/search",
                        "/api/v1/post/{slug}",
                        "/api/v1/post/latest",
                        "/api/v1/post/category/{slug}",
                        "/api/v1/category/**",
                        "/api/v1/tags/**",
                        "/api/v1/traffic/**",
                        "/api/v1/upload"
                    )
                    .permitAll()
                    // --- USERS (public profile) ---
                    .requestMatchers(
                        "/api/v1/user/profile/**",
                        "/api/v1/profile/**",
                        "/api/v1/users/top-authors",
                        "/api/v1/users/public/{username}"
                    )
                    .permitAll()
                    // --- NEWSLETTER ---
                    .requestMatchers(
                        "/api/v1/newsletter/subscribe",
                        "/api/v1/newsletter/confirm",
                        "/api/v1/newsletter/unsubscribe"
                    )
                    .permitAll()
                    // --- AUTHENTICATED USERS ---
                    .requestMatchers(
                        "/api/v1/author/**",
                        "/api/v1/user/profile",
                        "/api/v1/user/update-password",
                        "/api/v1/auth/me",
                        "/api/v1/auth/debug",
                        "/api/v1/auth/assign-default-role",
                        "/api/v1/jwt/roles",
                        "/posts/stats",
                        "/api/v1/comments/**",
                        "/api/v1/saved-posts/**",
                        "/api/v1/post/{postId}/bookmark/**",

                        "/api/v1/newsletter/subscribers/**",
                            "/api/v1/profile",
                        "/api/v1/notifications/**",
                         "/api/v1/post/{postId}/featured"
                    )
                    .authenticated()
                    // --- ROLE BASED ACCESS ---
                    .requestMatchers("/api/v1/user/**")
                    .hasRole("USER")
                    .requestMatchers("/api/v1/admin/users/**")
                    .hasRole("ADMIN")
                    // --- ANYTHING ELSE ---
                    .anyRequest()
                    .authenticated()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(
            List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:5000",
                "http://localhost:9090"
            )
        );
        config.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        );
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Cache preflight requests for 1 hour

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
