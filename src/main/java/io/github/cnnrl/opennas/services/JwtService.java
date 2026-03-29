package io.github.cnnrl.opennas.services;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.cnnrl.opennas.models.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
  private final SecretKey key;

  public JwtService(@Value("${jwt.secret}") String secret) {
    key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }

  public String tokenize(String username, Role role) {
    return Jwts.builder()
        .subject(username)
        .claim("role", role)
        .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24 Hours
        .signWith(key)
        .compact();

  }

  public String extractUsername(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload().getSubject();
  }

  public String extractRole(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("role", String.class);
  }

  public String generateStreamToken(String username, String songId, long duration) {
    return Jwts.builder()
        .subject(username)
        .claim("songId", songId)
        .expiration(new Date(System.currentTimeMillis() + (duration * 1000L) + 600000)) // duration + 10mins
        .signWith(key)
        .compact();
  }

  public String extractSongId(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("songId", String.class);
  }

  public Claims parse(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

}
