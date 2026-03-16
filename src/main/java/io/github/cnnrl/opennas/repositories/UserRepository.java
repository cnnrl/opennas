package io.github.cnnrl.opennas.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.cnnrl.opennas.models.User;

public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> getByUsername(String user);
}
