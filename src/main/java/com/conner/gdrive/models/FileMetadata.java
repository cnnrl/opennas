package com.conner.gdrive.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class FileMetadata {

  @Id
  private String id;

  private String fileName;
  private String contentType;
  private long fileSize;
  private String owner;

  public FileMetadata() {
    id = "";
    fileName = "";
    contentType = "";
    fileSize = 0;
    owner = "";
  }

  public FileMetadata(final String id, final String fileName, final String contentType, final long fileSize,
      final String owner) {
    this.id = id;
    this.fileName = fileName;
    this.contentType = contentType;
    this.fileSize = fileSize;
    this.owner = owner;
  }

  public String getId() {
    return id;
  }

  public String getFileName() {
    return fileName;
  }

  public String getContentType() {
    return contentType;
  }

  public long getFileSize() {
    return fileSize;
  }

  public String getOwner() {
    return owner;
  }

  public String toString() {
    return id + ", " + fileName + ", " + contentType + ", " + fileSize + ", " + owner;
  }
}
