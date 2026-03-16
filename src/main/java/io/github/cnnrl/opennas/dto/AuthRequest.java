package io.github.cnnrl.opennas.dto;

public class AuthRequest {
  private final String username;
  private final String password;

  public AuthRequest() {
    username = "";
    password = "";
  }

  public AuthRequest(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
