package com.learnforlearning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security setup: BCrypt passwords, a custom login page, and route-level
 * authorization. State-changing endpoints are POST-only and CSRF-protected.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // public assets and the PWA shell
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/icons/**",
                                "/manifest.webmanifest", "/service-worker.js", "/favicon.ico", "/offline")
                        .permitAll()
                        // public pages
                        .requestMatchers("/", "/register", "/login", "/error").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // browsing is public (GET only) — the matching POST endpoints
                        // (e.g. /subjects/add, /subject/vote) stay behind authentication
                        .requestMatchers(HttpMethod.GET, "/subjects", "/subjects/*",
                                "/subject/*/comments", "/fixable").permitAll()
                        // admin-only management area
                        .requestMatchers("/manage/**").hasRole("ADMIN")
                        // everything else needs a logged-in user
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/")
                        .permitAll())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // H2 console runs in a frame and posts without our CSRF token
                .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
