package io.github.cnnrl.opennas.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.github.cnnrl.opennas.dto.AuthRequest;
import io.github.cnnrl.opennas.services.AuthService;

@RestController
public class AuthController {
  private final AuthService authService;

  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody AuthRequest req) {
    try {
      authService.register(req.getUsername(), req.getPassword());
      return login(req);
    } catch (IllegalArgumentException e) {
      log.error("Could not create with username: {}", req.getUsername(), e);
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody AuthRequest req) {
    try {
      String token = authService.login(req.getUsername(), req.getPassword());
      return ResponseEntity
          .status(HttpStatus.OK)
          .body(token);
    } catch (IllegalArgumentException e) {
      log.error("Could not log in user: {}", req.getUsername(), e);
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

}
