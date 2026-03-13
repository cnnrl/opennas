package com.conner.gdrive.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.conner.gdrive.dto.DeleteTicket;
import com.conner.gdrive.dto.FileResult;
import com.conner.gdrive.models.FileMetadata;
import com.conner.gdrive.services.FileService;

@RestController
public class FileController {
  private final FileService fileService;

  private static final Logger log = LoggerFactory.getLogger(FileController.class);

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @GetMapping("/files")
  public ResponseEntity<?> listFiles() {
    String owner = SecurityContextHolder.getContext().getAuthentication().getName();
    try {
      List<FileMetadata> files = fileService.getFiles(owner);
      return ResponseEntity
          .status(HttpStatus.OK)
          .body(files);
    } catch (IllegalArgumentException e) {
      log.error("invalid owner param: {}", owner, e);
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (AccessDeniedException e) {
      log.error("Owner {}, unable to access file", owner, e);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      log.error("Unexpected error fetching files for owner {}", owner, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/files/{id}")
  public ResponseEntity<?> viewFile(@PathVariable String id) {
    String owner = SecurityContextHolder.getContext().getAuthentication().getName();
    try {
      FileResult fr = fileService.fetchFile(id, owner);
      Resource res = fr.getResource();
      FileMetadata md = fr.getMd();

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(md.getContentType()))
          .body(res);
    } catch (IllegalArgumentException e) {
      log.error("Invalid id: {}", id, e);
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (GeneralSecurityException e) {
      log.error("Cipher error decrypting file", e);
      return ResponseEntity.internalServerError().build();
    } catch (AccessDeniedException e) {
      log.error("Owner {}, unable to access file", owner, e);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (IOException e) {
      log.error("IO error fetching file with id: {}", id, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/download/{id}")
  public ResponseEntity<?> downloadFile(@PathVariable String id) {
    String owner = SecurityContextHolder.getContext().getAuthentication().getName();
    try {
      FileResult fr = fileService.fetchFile(id, owner);
      Resource res = fr.getResource();
      FileMetadata md = fr.getMd();

      String disposition = ContentDisposition.attachment()
          .filename(md.getFileName(), StandardCharsets.UTF_8)
          .build()
          .toString();

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .body(res);
    } catch (IllegalArgumentException e) {
      log.error("Invalid id: {} Owner: {}", id, owner, e);
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (AccessDeniedException e) {
      log.error("Owner unable to download file: {}", owner, e);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (IOException e) {
      log.error("IO error downloading file with id: {}", id, e);
      return ResponseEntity.internalServerError().build();
    } catch (GeneralSecurityException e) {
      log.error("Cipher error decrypting file", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping("/upload")
  public ResponseEntity<?> upload(@RequestParam MultipartFile file) {
    String owner = SecurityContextHolder.getContext().getAuthentication().getName();
    try {
      FileMetadata md = fileService.saveFile(file, owner);
      return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(md);
    } catch (IOException e) {
      log.error("IO error saving file for owner: {}", owner, e);
      return ResponseEntity.internalServerError().build();
    } catch (GeneralSecurityException e) {
      log.error("Cipher error encrypting file for owner: {}", owner, e);
      return ResponseEntity.internalServerError().build();
    } catch (IllegalArgumentException e) {
      log.error("Invalid upload request for owner {}", owner, e);
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<?> delete(@PathVariable String id) {
    try {
      DeleteTicket dt = fileService.delete(id);
      return ResponseEntity
          .status(HttpStatus.OK)
          .body(dt);
    } catch (IllegalArgumentException e) {
      log.error("Invalid delete request at id: {}", id, e);
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (IOException e) {
      log.error("Error deleting file at id: {}", id, e);
      return ResponseEntity.internalServerError().build();
    } catch (AccessDeniedException e) {
      log.error("Deleting is not permitted at id: {}", id, e);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
  }

}
