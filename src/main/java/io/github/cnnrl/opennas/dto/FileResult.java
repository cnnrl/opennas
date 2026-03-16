package io.github.cnnrl.opennas.dto;

import org.springframework.core.io.Resource;

import io.github.cnnrl.opennas.models.FileMetadata;

public class FileResult {
  private final Resource res;
  private final FileMetadata md;

  public FileResult(Resource res, FileMetadata md) {
    this.res = res;
    this.md = md;
  }

  public Resource getResource() {
    return res;
  }

  public FileMetadata getMd() {
    return md;
  }
}
