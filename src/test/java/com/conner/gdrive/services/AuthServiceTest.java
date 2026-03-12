package com.conner.gdrive.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.conner.gdrive.models.Role;
import com.conner.gdrive.models.User;
import com.conner.gdrive.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  UserRepository repo;

  @Mock
  BCryptPasswordEncoder passwordEncoder;

  @Mock
  JwtService jwtService;

  @InjectMocks
  AuthService authService;

  @Test
  void register_withValidInput_shouldSucceed() {
    authService.register("testname", "testpass");
    verify(repo).save(any(User.class));
  }

  @Test
  void register_withBlankUsername_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> {
      authService.register("", "testpass");
    });
    verify(repo, never()).save(any(User.class));
  }

  @Test
  void register_withDuplicateUsername_shouldThrowException() {
    when(repo.existsById("testname")).thenReturn(true);
    assertThrows(IllegalArgumentException.class, () -> {
      authService.register("testname", "testpass");
    });
    verify(repo, never()).save(any(User.class));
  }

  @Test
  void register_withBlankPassword_shoulThrowException() {
    assertThrows(IllegalArgumentException.class, () -> {
      authService.register("testname2", "");
    });
    verify(repo, never()).save(any(User.class));
  }

  @Test
  void login_withValid_Input_shouldSucceed() {
    User mockUser = new User("testname", "hashedpass", Role.USER);
    when(repo.getByUsername("testname")).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches("testpass", "hashedpass")).thenReturn(true);
    authService.login("testname", "testpass");
    verify(jwtService).tokenize(any(String.class), any(Role.class));
  }

  @Test
  void login_withBlankUsername_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> {
      authService.login("", "testpass");
    });
    verify(jwtService, never()).tokenize(any(String.class), any(Role.class));
  }

  @Test
  void login_withMissingUsername_shouldThrowException() {
    when(repo.getByUsername("fakename")).thenReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> {
      authService.login("fakename", "testpass");
    });
    verify(jwtService, never()).tokenize(any(String.class), any(Role.class));
  }

  @Test
  void login_withBlankPassword_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> {
      authService.login("testname", "");
    });
    verify(jwtService, never()).tokenize(any(String.class), any(Role.class));
  }

  @Test
  void login_withWrongPassword_shouldThrowException() {
    User mockUser = new User("testname", "hashedpass", Role.USER);
    when(repo.getByUsername("testname")).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches("wrongpass", "hashedpass")).thenReturn(false);
    assertThrows(IllegalArgumentException.class, () -> {
      authService.login("testname", "wrongpass");
    });
    verify(jwtService, never()).tokenize(any(String.class), any(Role.class));
  }
}
