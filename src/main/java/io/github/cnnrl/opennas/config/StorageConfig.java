package io.github.cnnrl.opennas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {
  private final String storagePath;

  public StorageConfig(@Value("${opennas.storage.path}") String storagePath) {
    this.storagePath = storagePath;
  }

  public String getStoragePath() {
    return storagePath;
  }
}
