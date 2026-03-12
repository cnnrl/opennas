package com.conner.gdrive.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.conner.gdrive.models.FileMetadata;
import com.conner.gdrive.repositories.FileMetadataRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CleanerService {
  private final FileMetadataRepository repo;
  private static final Logger log = LoggerFactory.getLogger(CleanerService.class);

  public CleanerService(FileMetadataRepository repo) {
    this.repo = repo;
  }

  // @Scheduled(cron = "0 0 0 * * * ") For prod - midnight
  @Scheduled(fixedRate = 180000) // For test - 3 mins
  public void clean() {
    Set<String> dbIds = repo.findAll().stream()
        .map(FileMetadata::getId)
        .collect(Collectors.toCollection(HashSet::new));
    try (Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.home"), "gdrive"))) {
      paths.filter(p -> p.toFile().isFile())
          .filter(p -> !p.startsWith(Paths.get(System.getProperty("user.home"), "gdrive", "glogs")))
          .forEach(p -> {
            String id = p.getParent().getFileName().toString();
            try {
              UUID.fromString(id);
            } catch (Exception e) {
              log.error("Cleaner failed!", e);
            }
            if (dbIds.contains(id)) {
              dbIds.remove(id);
            } else {
              try {
                Path idDir = p.getParent();
                Files.deleteIfExists(p);
                Files.deleteIfExists(idDir);
              } catch (IOException e) {
                log.error("Cleaner failed!", e);
              }
            }
          });
    } catch (IOException e) {
      log.error("Cleaner failed!", e);
    }
    repo.deleteAllById(dbIds);
  }
}
