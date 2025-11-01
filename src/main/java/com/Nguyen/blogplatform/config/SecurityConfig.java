package com.Nguyen.blogplatform.config;

import com.Nguyen.blogplatform.exception.CustomAccessDeniedHandler;
import com.Nguyen.blogplatform.security.AuthEntryPointJwt;
import com.Nguyen.blogplatform.security.AuthTokenFilter;
import com.Nguyen.blogplatform.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthTokenFilter jwtAuthFilter;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth

                        // --- PUBLIC ENDPOINTS ---
                        .requestMatchers(
                                "/", "/actuator/**", "/images/**", "/uploads/**", "/uploads/thumbnail/**",
                                "/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**", "/api-docs/**",
                                "/ws/**", "/ws-logs/**", "/video/**", "/logs/**", "/logger/**"
                        ).permitAll()

                        // --- AUTH & UTILITY ---
                        .requestMatchers(
                                "/api/v1/auth/refresh-token",
                                "/api/v1/auth/**",

                                "/api/v1/jwt/decode",
                                "/api/v1/jwt/validate",
                                "/api/debug/**"
                        ).permitAll()
//                        .requestMatchers("/api/v1/series",
//                                "/api/v1/series/*/slug/*",
//                                        "/api/v1series/popular"
//                        ).permitAll()

                        .requestMatchers("/api/v1/series").permitAll()
                        .requestMatchers("/api/v1/series/**").permitAll()

                        // --- POSTS / TAGS / CATEGORIES (public GET) ---
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/post",
                                "/api/v1/post/featured",
                                "/api/v1/post/search",
                                "/api/v1/post/{slug}",
                                "/api/v1/post/latest",
                                "/api/v1/post/category/{categoryId}",
                                "/api/v1/category/**",
                                "/api/v1/tags/**"
                        ).permitAll()

                        // --- USERS (public profile) ---
                        .requestMatchers(
                                "/api/v1/users/profile/**",
                                "/api/v1/users/top-authors",
                                "/api/v1/user/public/{username}"
                        ).permitAll()

                        // --- NEWSLETTER ---
                        .requestMatchers(
                                "/api/v1/newsletter/subscribe",
                                "/api/v1/newsletter/confirm",
                                "/api/v1/newsletter/unsubscribe"
                        ).permitAll()

                        // --- AUTHENTICATED USERS ---
                        .requestMatchers(
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
                                "/api/v1/profile/custom"
                        ).authenticated()

                        // --- ROLE BASED ACCESS ---
                        .requestMatchers("/api/v1/user/**").hasRole("USER")
                        .requestMatchers("/api/v1/admin/users/**").hasRole("ADMIN")

                        // --- ANYTHING ELSE ---
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:5000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
