package io.github.cnnrl.opennas.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.cnnrl.opennas.models.AuditEntry;

import tools.jackson.databind.ObjectMapper;

@Service
public class AuditService {
  private final ObjectMapper mapper;
  private static final Logger log = LoggerFactory.getLogger(FileService.class);

  public AuditService(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public void log(AuditEntry entry) {
    Path storagePath = Paths.get(System.getProperty("user.home"), "opennas", "logs", LocalDate.now().toString() + ".json");

    try {
      String json = mapper.writeValueAsString(entry);
      Files.createDirectories(storagePath.getParent());

      Files.write(storagePath, (json + "\n").getBytes(),
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
    } catch (Exception e) {
      log.error("Failed to write audit log", e);
    }
  }
}
