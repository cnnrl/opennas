package com.conner.gdrive.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.conner.gdrive.models.SongMetadata;
import com.conner.gdrive.services.MusicService;

@RestController
public class MusicController {
  private final MusicService musicService;

  private static final Logger log = LoggerFactory.getLogger(MusicController.class);

  public MusicController(MusicService musicService) {
    this.musicService = musicService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/music/upload")
  public ResponseEntity<?> uploadMusic(@RequestParam MultipartFile file) {
    String owner = SecurityContextHolder.getContext().getAuthentication().getName();
    try {
      SongMetadata md = musicService.upload(file);
      return ResponseEntity.status(HttpStatus.CREATED).body(md);
    } catch (IllegalArgumentException e) {
      log.error("User: {}, error sending file", owner, e);
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      log.error("Error uploading file, req by {}", owner, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/stream/{id}")
  public ResponseEntity<?> getStreamToken(@PathVariable String id) {
    String owner = SecurityContextHolder.getContext().getAuthentication().getName();
    try {
      String token = musicService.getStreamToken(id, owner);
      return ResponseEntity.status(HttpStatus.OK).body(token);
    } catch (IllegalArgumentException e) {
      log.error("Could not get token for owner: {} for song: {}", owner, id, e);
      return ResponseEntity.badRequest().body(e.getMessage());
    }

  }

}
