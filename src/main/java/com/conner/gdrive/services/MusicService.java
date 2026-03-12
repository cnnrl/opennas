package com.conner.gdrive.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.conner.gdrive.models.SongMetadata;
import com.conner.gdrive.repositories.SongRepository;

@Service
public class MusicService {
  private final SongRepository repo;
  private final JwtService jwtService;

  public MusicService(SongRepository repo, JwtService jwtService) {
    this.repo = repo;
    this.jwtService = jwtService;
  }

  public SongMetadata upload(MultipartFile file) throws Exception {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }
    String id = UUID.randomUUID().toString();
    Path tempPath = Paths.get(System.getProperty("user.home"), "gdrive", "temp", id, file.getOriginalFilename());

    Files.createDirectory(tempPath.getParent());
    Files.copy(file.getInputStream(), tempPath);

    AudioFile audioFile = AudioFileIO.read(tempPath.toFile());
    AudioHeader header = audioFile.getAudioHeader();
    int duration = header.getTrackLength();
    Tag tag = audioFile.getTag();
    String title = tag.getFirst(FieldKey.TITLE);
    String artist = tag.getFirst(FieldKey.ARTIST);
    String album = tag.getFirst(FieldKey.ALBUM);
    Artwork cover = tag.getFirstArtwork();
    String ext = "";

    Path songPath = Paths.get(System.getProperty("user.home"), "gdrive", "music", id, file.getOriginalFilename());
    Files.createDirectories(songPath.getParent());

    if (cover != null) {
      String mimeType = cover.getMimeType();
      ext = mimeType.contains("png") ? "png" : "jpg";

      Path coverPath = Paths.get(System.getProperty("user.home"), "gdrive", "music", id, "cover." + ext);
      Files.write(coverPath, cover.getBinaryData());
    }

    SongMetadata md = new SongMetadata(id, title, artist, album, duration, file.getSize(), ext);
    repo.save(md);

    try {
      Files.move(tempPath, songPath);
      Files.delete(tempPath.getParent());
    } catch (Exception e) {
      repo.deleteById(id);
      Files.deleteIfExists(songPath.getParent().resolve("cover." + ext));
      Files.deleteIfExists(songPath.getParent());

      throw e;
    }

    return md;
  }

  public String getStreamToken(String id, String username) {
    SongMetadata md = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Song not found!"));
    return jwtService.generateStreamToken(username, id, md.getDuration());
  }
}
