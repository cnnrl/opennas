package io.github.cnnrl.opennas.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
  @Id
  private String username;

  private String password;

  @Enumerated(EnumType.STRING)
  private Role role;

  public User() {
    username = "";
    password = "";
    role = Role.USER;
  }

  public User(String username, String passwd, Role role) {
    this.username = username;
    this.password = passwd;
    this.role = role;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public Role getRole() {
    return role;
  }
}
