package io.github.cnnrl.opennas.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.cnnrl.opennas.config.StorageConfig;
import io.github.cnnrl.opennas.models.AuditEntry;

import tools.jackson.databind.ObjectMapper;

@Service
public class AuditService {
  private final ObjectMapper mapper;
  private final String globalPath;
  private static final Logger log = LoggerFactory.getLogger(FileService.class);

  public AuditService(ObjectMapper mapper, StorageConfig storageConfig) {
    this.mapper = mapper;
    globalPath = storageConfig.getStoragePath();
  }

  public void log(AuditEntry entry) {
    Path storagePath = Paths.get(globalPath, "logs", LocalDate.now().toString() + ".json");

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
