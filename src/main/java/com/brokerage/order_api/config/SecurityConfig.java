package com.brokerage.order_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// Tells spring that this class is used as a config source like Startup.cs
@Configuration
public class SecurityConfig {
    // Managed beans are like adding services to DI container (services.AddSingleton..)
    @Bean
    public  SecurityFilterChain filterChain(HttpSecurity http) throws  Exception {

        http
            .csrf().disable()
            .headers().disable()
            .authorizeHttpRequests()
            .anyRequest().permitAll();
        return http.build();
    }
}
