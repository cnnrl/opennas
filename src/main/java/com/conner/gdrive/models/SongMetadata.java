package com.conner.gdrive.models;

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
  private String coverExt;

  public String getCoverExt() {
    return coverExt;
  }

  public SongMetadata(String id, String songName, String artist, String album, int duration, long fileSize,
      String coverExt) {
    this.id = id;
    this.songName = songName;
    this.artist = artist;
    this.album = album;
    this.duration = duration;
    this.fileSize = fileSize;
    this.coverExt = coverExt;
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
}
