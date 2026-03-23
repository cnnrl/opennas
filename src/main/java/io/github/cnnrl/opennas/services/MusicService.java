package io.github.cnnrl.opennas.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.github.cnnrl.opennas.config.StorageConfig;
import io.github.cnnrl.opennas.models.SongMetadata;
import io.github.cnnrl.opennas.repositories.SongRepository;

import io.jsonwebtoken.Claims;

@Service
public class MusicService {
  private final SongRepository repo;
  private final JwtService jwtService;
  private final String globalPath;
  private final Path NO_COVER_PATH;

  public MusicService(SongRepository repo, JwtService jwtService, StorageConfig storageConfig) {
    this.repo = repo;
    this.jwtService = jwtService;
    globalPath = storageConfig.getStoragePath();
    NO_COVER_PATH = Paths.get(globalPath, "tmp", "cover.jpg");
  }

  public SongMetadata upload(MultipartFile file) throws Exception {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }
    String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
    String id = UUID.randomUUID().toString();
    Path tempPath = Paths.get(globalPath, "temp", id, fileName);

    Files.createDirectories(tempPath.getParent());
    Files.copy(file.getInputStream(), tempPath);

    AudioFile audioFile = AudioFileIO.read(tempPath.toFile());
    AudioHeader header = audioFile.getAudioHeader();
    int duration = header.getTrackLength();
    Tag tag = audioFile.getTag();
    String title = tag != null ? tag.getFirst(FieldKey.TITLE) : "";
    String artist = tag != null ? tag.getFirst(FieldKey.ARTIST) : "";
    String album = tag != null ? tag.getFirst(FieldKey.ALBUM) : "";
    Artwork cover = tag != null ? tag.getFirstArtwork() : null;
    String format = header.getFormat().toLowerCase();
    String mimeType = switch (format) {
      case "mp3", "mpeg" -> "audio/mpeg";
      case "flac" -> "audio/flac";
      case "ogg" -> "audio/ogg";
      case "mp4", "m4a", "aac" -> "audio/mp4";
      case "wav" -> "audio/wav";
      default -> "audio/mpeg";
    };
    String ext = "";
    String coverType = "image/jpeg";

    Path songPath = Paths.get(globalPath, "music", id, fileName);
    Files.createDirectories(songPath.getParent());

    if (cover != null) {
      coverType = cover.getMimeType();
      if (coverType != null && coverType.contains("/")) {
        ext = "." + coverType.substring(coverType.indexOf("/") + 1).toLowerCase();
      } else {
        ext = ".jpg";
      }

      Path coverPath = Paths.get(globalPath, "music", id, "cover" + ext);
      Files.write(coverPath, cover.getBinaryData());
    }

    SongMetadata md = new SongMetadata(id, title, artist, album, duration, file.getSize(),
        file.getOriginalFilename().toString(), ext, mimeType, coverType);
    repo.save(md);

    try {
      Files.move(tempPath, songPath, StandardCopyOption.REPLACE_EXISTING);
      Files.deleteIfExists(tempPath);
      Files.deleteIfExists(tempPath.getParent());
    } catch (Exception e) {
      repo.deleteById(id);
      Files.deleteIfExists(songPath.getParent().resolve("cover" + ext));
      Files.deleteIfExists(songPath.getParent());

      throw e;
    }

    return md;
  }

  public List<SongMetadata> findSongs() {
    return repo.findAll();
  }

  public List<SongMetadata> findByAlbum(String album) {
    return repo.findByAlbum(album);
  }

  public Resource getCover(String id) throws IOException {
    SongMetadata md = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Song not found!"));

    Path path = Paths.get(globalPath, "music", id, "cover" + md.getCoverExt());
    if (md.getCoverExt().isBlank() || !Files.exists(path)) {
      path = NO_COVER_PATH;
    }

    Resource cover = new UrlResource(path.toUri());

    return cover;
  }

  public String getStreamToken(String id, String username) {
    SongMetadata md = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Song not found!"));
    return jwtService.generateStreamToken(username, id, md.getDuration());
  }

  public Resource getSongResource(String id, String token, String currUser) throws IOException {
    Claims claims = jwtService.parse(token);
    if (!currUser.equals(claims.getSubject())) {
      throw new IllegalArgumentException("Token user mismatch!");
    }

    if (!id.equals(claims.get("songId", String.class))) {
      throw new IllegalArgumentException("You are not allowed to stream this song!");
    }

    SongMetadata md = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Song not found!"));

    Path storagePath = Paths.get(globalPath, "music", id, md.getFileName());

    if (!Files.exists(storagePath)) {
      throw new IllegalArgumentException("File missing!");
    }

    return new UrlResource(storagePath.toUri());
  }

  public long getTotalSize(String id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Song not found!")).getFileSize();
  }

  public String getSongMimeType(String id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Song not found!")).getSongMimeType();
  }

  public String getCoverExt(String id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Song not found!")).getCoverExt();
  }

  public String getCoverMimeType(String id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Song not found!")).getCoverMimeType();
  }
}
