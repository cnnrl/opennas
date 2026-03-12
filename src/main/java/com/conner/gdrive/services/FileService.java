package com.conner.gdrive.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.conner.gdrive.dto.FileResult;
import com.conner.gdrive.models.FileMetadata;
import com.conner.gdrive.models.DeleteTicket;
import com.conner.gdrive.repositories.FileMetadataRepository;

@Service
public class FileService {
  private final FileMetadataRepository repo;
  private final FileEncryptService fes;

  public FileService(FileMetadataRepository repo, FileEncryptService fes) {
    this.repo = repo;
    this.fes = fes;
  }

  public FileMetadata saveFile(MultipartFile file, String owner) throws GeneralSecurityException, IOException {

    if (file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }

    if (owner == null || owner.isBlank()) {
      throw new IllegalArgumentException("User is empty or null");
    }

    String id = UUID.randomUUID().toString();
    String home = System.getProperty("user.home");
    String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
    Path storagePath = Paths.get(home, "gdrive", owner, id, fileName);

    String type = file.getContentType();

    if (type == null) {
      type = "application/octet-stream";
    }

    FileMetadata fileMD = new FileMetadata(id, fileName, type, file.getSize(), owner);
    repo.save(fileMD);
    try {
      Files.createDirectories(storagePath.getParent());
    } catch (IOException e) {
      repo.deleteById(id);
      throw e;
    }
    try (
        InputStream fileStream = file.getInputStream();
        OutputStream diskStream = Files.newOutputStream(storagePath);) {
      fes.encrypt(fileStream, diskStream);
    } catch (Exception e) {
      repo.deleteById(id);
      throw e;
    }
    return fileMD;

  }

  public List<FileMetadata> getFiles(String owner) {
    return repo.findByOwner(owner);
  }

  public FileResult fetchFile(String id, String owner) throws IOException, GeneralSecurityException {

    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("ID is empty or null");
    }

    FileMetadata md = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("File Not Found!"));
    if (!md.getOwner().equals(owner)) {
      throw new AccessDeniedException("Access denied");
    }

    String fileName = Paths.get(md.getFileName()).getFileName().toString();
    Path path = Paths.get(System.getProperty("user.home"), "gdrive", md.getOwner(), id, fileName);

    if (!Files.exists(path)) {
      throw new FileNotFoundException("File Not Found!");
    }

    InputStream in = Files.newInputStream(path);
    InputStream dec = fes.decrypt(in);
    Resource res = new InputStreamResource(dec);

    return new FileResult(res, md);

  }

  public DeleteTicket delete(String id) throws IOException {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("ID is empty or null");
    }

    FileMetadata md = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("File Not Found!"));

    Path path = Paths.get(System.getProperty("user.home"), "gdrive", md.getOwner(), id, md.getFileName());
    if (!Files.exists(path)) {
      throw new FileNotFoundException("File Not Found!");
    }
    Files.delete(path);
    Files.delete(path.getParent());
    repo.deleteById(id);
    return DeleteTicket.of(md.getId(), md.getOwner(), md.getFileName(), md.getFileSize());
  }
}
