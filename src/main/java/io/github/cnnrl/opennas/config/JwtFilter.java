package io.github.cnnrl.opennas.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.cnnrl.opennas.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  protected void doFilterInternal(HttpServletRequest req,
      HttpServletResponse res,
      FilterChain filterChain) throws ServletException, IOException {
    String authHeader = req.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(req, res);
      return;
    }

    String token = authHeader.substring(7);
    String username = jwtService.extractUsername(token);
    List<GrantedAuthority> authorities = List.of(
        new SimpleGrantedAuthority("ROLE_" + jwtService.extractRole(token)));

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        username, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(auth);

    filterChain.doFilter(req, res);
  }
}
