package com.ust.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@PropertySource(value = "classpath:application-dev.properties", ignoreResourceNotFound = true)
public class SecurityConfig {

    private final com.ust.backend.auth.JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(com.ust.backend.auth.JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // allow CORS preflight
                        .requestMatchers("/", "/health", "/auth/login").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**", "/api/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(Environment env) {
        String allowedOrigin = env.getProperty("ust.cors.allowed-origin", "http://localhost:3000");
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Dev-only in-memory users loaded from application-dev.properties
    @Bean
    @Profile("dev")
    @ConditionalOnProperty(name = {"ust.app.user", "ust.app.password"})
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        try {
            java.util.Properties props = new java.util.Properties();
            org.springframework.core.io.ClassPathResource res = new org.springframework.core.io.ClassPathResource("application-dev.properties");
            try (java.io.InputStream is = res.getInputStream()) {
                props.load(is);
            }
            String username = props.getProperty("ust.app.user");
            String rawPassword = props.getProperty("ust.app.password");
            UserDetails user = User
                    .withUsername(username)
                    .password(passwordEncoder.encode(rawPassword))
                    .roles("USER")
                    .build();

            String adminUsername = props.getProperty("ust.app.admin.user");
            String adminRawPassword = props.getProperty("ust.app.admin.password");
            if (adminUsername != null && adminRawPassword != null) {
                UserDetails admin = User
                        .withUsername(adminUsername)
                        .password(passwordEncoder.encode(adminRawPassword))
                        .roles("ADMIN")
                        .build();
                return new InMemoryUserDetailsManager(user, admin);
            }
            return new InMemoryUserDetailsManager(user);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load dev credentials from application-dev.properties", ex);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
