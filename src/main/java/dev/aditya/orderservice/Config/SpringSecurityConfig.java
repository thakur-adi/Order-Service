package dev.aditya.orderservice.Config;

import dev.aditya.orderservice.Security.AuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SpringSecurityConfig {
    @Autowired
    private AuthFilter authFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
        httpSecurity.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.PUT,"/payment-status").permitAll() // Only put in servlet path no need for context path.
                        .anyRequest().authenticated())
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf-> csrf.disable());
        return httpSecurity.build();
    }
}