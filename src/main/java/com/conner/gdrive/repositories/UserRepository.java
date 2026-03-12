package com.conner.gdrive.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conner.gdrive.models.User;

public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> getByUsername(String user);
}
