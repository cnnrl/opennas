package io.github.cnnrl.opennas.controllers;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.github.cnnrl.opennas.models.SongMetadata;
import io.github.cnnrl.opennas.services.MusicService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class MusicController {
  private final MusicService musicService;

  private static final Logger log = LoggerFactory.getLogger(MusicController.class);

  public MusicController(MusicService musicService) {
    this.musicService = musicService;
  }

  @GetMapping("/music")
  public ResponseEntity<?> viewSongs() {
    List<SongMetadata> songs = musicService.findSongs();
    return ResponseEntity.ok().body(songs);
  }

  @GetMapping("/music/{album}")
  public ResponseEntity<?> viewAlbum(@PathVariable String album) {
    List<SongMetadata> songs = musicService.findByAlbum(album);
    return ResponseEntity.ok().body(songs);
  }

  // @PreAuthorize("hasRole('ADMIN')")
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

  @GetMapping("/music/art/{id}")
  public ResponseEntity<?> getArt(@PathVariable String id) {
    String user = SecurityContextHolder.getContext().getAuthentication().getName();
    try {
      Resource res = musicService.getCover(id);
      return ResponseEntity.ok().body(res);
    } catch (IllegalArgumentException e) {
      log.error("User: {} Could not get cover for song: {}", user, id, e);
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (IOException e) {
      log.error("Could not send cover for song: {}", id, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/stream/token/{id}")
  public ResponseEntity<?> getStreamToken(@PathVariable String id, HttpServletRequest request) {
    String owner = SecurityContextHolder.getContext().getAuthentication().getName();
    String sessionId = request.getSession().getId();

    try {
      String token = musicService.getStreamToken(id, owner, sessionId);
      return ResponseEntity.status(HttpStatus.OK).body(token);
    } catch (IllegalArgumentException e) {
      log.error("Could not get token for owner: {} for song: {}", owner, id, e);
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping(value = "/stream/{id}")
  public ResponseEntity<ResourceRegion> streamSong(@PathVariable String id, @RequestParam String token,
      @RequestHeader HttpHeaders headers, HttpServletRequest req) {
    String user = SecurityContextHolder.getContext().getAuthentication().getName();
    String session = req.getSession().getId();
    try {
      Resource resource = musicService.getSongResource(id, token, user, session);
      long contentLength = resource.contentLength();
      String mimeType = musicService.getMimeType(id);

      List<HttpRange> ranges = headers.getRange();

      if (ranges.isEmpty()) {
        ResourceRegion region = new ResourceRegion(resource, 0, Math.min(1024 * 1024, contentLength));
        return ResponseEntity.ok()
            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
            .contentType(MediaType.parseMediaType(mimeType))
            .body(region);
      }

      HttpRange range = ranges.get(0);
      ResourceRegion region = range.toResourceRegion(resource);

      return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
          .header(HttpHeaders.ACCEPT_RANGES, "bytes")
          .contentType(MediaType.parseMediaType(mimeType))
          .body(region);

    } catch (IOException e) {
      log.error("");
      return ResponseEntity.internalServerError().build();
    } catch (IllegalArgumentException e) {
      log.error("Malformed request for song ID: {}", id, e);
      return ResponseEntity.internalServerError().build();
    }
  }

}
