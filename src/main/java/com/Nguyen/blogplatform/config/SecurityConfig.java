package com.Nguyen.blogplatform.config;

import com.Nguyen.blogplatform.exception.CustomAccessDeniedHandler;
import com.Nguyen.blogplatform.security.AuthEntryPointJwt;
import com.Nguyen.blogplatform.security.AuthTokenFilter;
import com.Nguyen.blogplatform.security.OAuth2AuthenticationFailureHandler;
import com.Nguyen.blogplatform.security.OAuth2AuthenticationSuccessHandler;
import com.Nguyen.blogplatform.service.auth.CustomOAuth2UserService;
import com.Nguyen.blogplatform.service.auth.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthTokenFilter        jwtAuthFilter;
    private final AuthEntryPointJwt      unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            UserDetailsServiceImpl userDetailsService,
            AuthTokenFilter jwtAuthFilter,
            AuthEntryPointJwt unauthorizedHandler,
            CustomAccessDeniedHandler accessDeniedHandler,
            CustomOAuth2UserService customOAuth2UserService,
            @Lazy OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
            OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
            PasswordEncoder passwordEncoder
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.unauthorizedHandler = unauthorizedHandler;
        this.accessDeniedHandler = accessDeniedHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.passwordEncoder = passwordEncoder;
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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth

                        // --- Infrastructure & docs ---
                        .requestMatchers(PUBLIC_INFRA).permitAll()

                        // --- Auth endpoints ---
                        .requestMatchers(PUBLIC_AUTH).permitAll()

                        // --- Public GET: posts / search / categories / tags ---
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_CONTENT).permitAll()

                        // --- Public: user profiles ---
                        .requestMatchers(PUBLIC_USER_PROFILES).permitAll()

                        // --- Public: newsletter opt-in ---
                        .requestMatchers(PUBLIC_NEWSLETTER).permitAll()

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
        config.setAllowedOrigins(ALLOWED_ORIGINS);
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
            "/actuator/**",
            "/images/**",
            "/uploads/**",
            "/uploads/thumbnail/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/ws/**",
            "/ws-logs/**",
            "/video/**",
            "/logs/**",
            "/logger/**",
            "/traffic",
            "/profile/avatar/**"
    };

    private static final String[] PUBLIC_AUTH = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/jwt/decode",
            "/api/v1/jwt/validate",
            "/auth/**",
            "/oauth2/**",
            "/login/oauth2/**",
            "/api/v1/oauth/**",
            "/api/v1/series/**"
    };

    private static final String[] PUBLIC_GET_CONTENT = {
            "/api/v1/post",
            "/api/v1/post/featured",
            "/api/v1/post/search",
            "/api/v1/post/latest",
            "/api/v1/post/{slug}",
            "/api/v1/post/category/{slug}",
            "/api/v1/posts/{postId}/comments",   // read comments is public
            "/api/v1/comments/{commentId}/replies",
            "/api/v1/search",
            "/api/v1/category/**",
            "/api/v1/tags/**",
            "/api/v1/traffic/**"
    };

    private static final String[] PUBLIC_USER_PROFILES = {
            "/api/v1/users/profile/{slug}",
            "/api/v1/user/profile/**",
            "/api/v1/profile/**",
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
            "/api/v1/user/update-password",
            "/api/v1/profile",
            "/api/v1/jwt/roles",
            "/api/v1/posts/{postId}/comments",   // write comment requires auth
            "/api/v1/comments/**",
            "/api/v1/saved-posts/**",
            "/api/v1/post/{postId}/bookmark/**",
            "/api/v1/post/{postId}/featured",
            "/api/v1/newsletter/subscribers/**",
            "/api/v1/notifications/**"
    };

    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:5000",
            "http://localhost:9090"
            // Thêm production domain vào đây — không hardcode ngrok URL
    );
}