package io.github.cnnrl.opennas.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  private final JwtFilter jwtFilter;
  private final String corsOrigin;

  public SecurityConfig(JwtFilter jwtFilter, @Value("${opennas.cors.origin}") String corsOrigin) {
    this.jwtFilter = jwtFilter;
    this.corsOrigin = corsOrigin;
  }

  @Bean
  public SecurityFilterChain filter(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers.frameOptions(frame -> frame.disable()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/register", "/login").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    http.cors(cors -> cors.configurationSource(request -> {
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowedOrigins(List.of(corsOrigin));
      config.setAllowedMethods(List.of("GET", "POST", "DELETE"));
      config.setAllowedHeaders(List.of("*"));
      config.setAllowCredentials(true);
      return config;
    }));
    return http.build();
  }

  @Bean
  @Order(1)
  public SecurityFilterChain h2ConsoleFilter(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/h2-console/**")
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers.frameOptions(frame -> frame.disable()))
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
