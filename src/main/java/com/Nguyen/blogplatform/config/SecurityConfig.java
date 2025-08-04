package com.Nguyen.blogplatform.config;

import com.Nguyen.blogplatform.Enum.ERole;
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
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    private AuthTokenFilter jwtAuthFilter;
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;



    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
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
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/uploads/thumbnail/**").permitAll()
                        .requestMatchers("/api/v1/upload/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/ws-logs/**").permitAll()
                        .requestMatchers("/actuator/**",
                            "/actuator/prometheus"
                        ).permitAll()
                        .requestMatchers("/api/v1/post/latest").permitAll()
                        .requestMatchers("/api/logs").permitAll()
                        .requestMatchers("/logs/**").permitAll()
                        .requestMatchers("/logger/**").permitAll()
                        .requestMatchers("/api/v1/tags/**").permitAll()
                        // Public endpoint
                        .requestMatchers(
                                "/",
                                "/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/api/test/**",
                                "/api/v1/auth/fix-roles",
                                "/api/v1/user/forgot-password"

                        ).permitAll()
                        .requestMatchers("/api/users/profile").authenticated()
                        .requestMatchers("/api/v1/auth/fix-roles").permitAll()
                        .requestMatchers("/api/v1/auth/me").authenticated()
                        .requestMatchers("/api/v1/auth/debug").authenticated()
                        .requestMatchers("/api/v1/auth/assign-default-role").authenticated()
                        .requestMatchers("/api/debug/**").permitAll()
                        .requestMatchers("/api/v1/auth/profile/**").permitAll()
                        .requestMatchers("/video/**").permitAll()
                        .requestMatchers("/api/v1/memes/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/v1/category").permitAll()
                         // Public GET endpoints
                         .requestMatchers(HttpMethod.GET,
                                 "/api/v1/post",
                                 "/api/v1/post/featured",
                                 "/api/v1/post/search",
                                 "/api/v1/post/{slug}",
                                 "/api/v1/post/category/{categoryId}",
                                 "/api/v1/category/**"
                         ).permitAll()
                        .requestMatchers("/api/v1/users/profile/{username}").permitAll()

                        .requestMatchers("/api/v1/users/profile/**").permitAll()
                         // Các rule khác giữ nguyên
                        .requestMatchers("/api/v1/upload").permitAll()
                        .requestMatchers("/api/v1/user/update-password").authenticated()
                        .requestMatchers("/api/v1/user/**").hasRole("USER")
                        .requestMatchers("/api/user/{id}").permitAll()

                        .requestMatchers("/api/v1/author/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/comments/**").authenticated()
                        .requestMatchers("/api/v1/role/**").permitAll()
                        .requestMatchers("/api/v1/roles/**").permitAll()
                         // Newsletter endpoints
                        .requestMatchers("/api/v1/newsletter/subscribe").permitAll()
                        .requestMatchers("/api/v1/newsletter/confirm").permitAll()
                        .requestMatchers("/api/v1/newsletter/unsubscribe").permitAll()
                        .requestMatchers("/api/v1/newsletter/subscribers/**").hasRole("ADMIN")
                         // Saved posts endpoints
                         .requestMatchers("/api/v1/saved-posts/**").authenticated()
                         // JWT utility endpoints
                        .requestMatchers("/api/v1/jwt/decode").permitAll()
                        .requestMatchers("/api/v1/jwt/validate").permitAll()
                        .requestMatchers("/api/v1/jwt/roles").authenticated()
                        .anyRequest().authenticated()

                )
                // .csrf(AbstractHttpConfigurer::disable)
                // .cors(configurer -> corsConfigurationSource())
//                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        http.authenticationProvider(authenticationProvider());
        // http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:3000",
                                                "http://localhost:5173/",
                                                "http://localhost:5174/",
                                                "http://localhost:5000/"
                )
        );
        // Use allowedOrigins() if you only have exact values
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // Required if sending cookies/token

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

}
