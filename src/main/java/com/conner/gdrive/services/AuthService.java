package com.conner.gdrive.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.conner.gdrive.models.Role;
import com.conner.gdrive.models.User;
import com.conner.gdrive.repositories.UserRepository;

@Service
public class AuthService {
  private final UserRepository repo;
  private final BCryptPasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(UserRepository repo, BCryptPasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.repo = repo;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public void register(String username, String password) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("Username is empty or null!");
    }

    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("Password is empty or null!");
    }

    if (repo.existsById(username)) {
      throw new IllegalArgumentException("That username is taken!");
    }

    User user = new User(username, passwordEncoder.encode(password), Role.USER);
    repo.save(user);
  }

  public String login(String username, String password) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("Username is empty or null!");
    }

    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("Password is empty or null!");
    }

    User user = repo.getByUsername(username).orElseThrow(() -> new IllegalArgumentException("Username is incorrect!"));
    if (passwordEncoder.matches(password, user.getPassword())) {
      return jwtService.tokenize(username, user.getRole());
    } else {
      throw new IllegalArgumentException("Password does not match!");
    }
  }
}
