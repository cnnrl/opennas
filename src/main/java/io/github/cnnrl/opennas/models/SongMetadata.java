package io.github.cnnrl.opennas.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SongMetadata {
  @Id
  private String id;

  private String songName;
  private String artist;
  private String album;
  private int duration;
  private long fileSize;
  private String fileName;
  private String coverExt;
  private String songMimeType;
  private String coverMimeType;

  public SongMetadata() {
    this("", "", "", "", 0, 0L, "", "", "audo/mpeg", "image/jpeg");
  }

  public SongMetadata(String id, String songName, String artist, String album, int duration, long fileSize,
      String fileName, String coverExt, String songMimeType, String coverMimeType) {
    this.id = id;
    this.songName = songName;
    this.artist = artist;
    this.album = album;
    this.duration = duration;
    this.fileSize = fileSize;
    this.fileName = fileName;
    this.coverExt = coverExt;
    this.songMimeType = songMimeType;
    this.coverMimeType = coverMimeType;
  }

  public String getId() {
    return id;
  }

  public String getSongName() {
    return songName;
  }

  public String getArtist() {
    return artist;
  }

  public String getAlbum() {
    return album;
  }

  public int getDuration() {
    return duration;
  }

  public long getFileSize() {
    return fileSize;
  }

  public String getFileName() {
    return fileName;
  }

  public String getCoverExt() {
    return coverExt;
  }

  public String getSongMimeType() {
    return songMimeType;
  }

  public String getCoverMimeType() {
    return coverMimeType;
  }

}
