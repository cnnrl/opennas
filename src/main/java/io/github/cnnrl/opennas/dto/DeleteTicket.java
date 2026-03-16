package io.github.cnnrl.opennas.dto;

import java.time.LocalDateTime;

public record DeleteTicket(String time, String id, String owner, String filename, long fileSize) {
  public static DeleteTicket of(String id, String owner, String filename, long fileSize) {
    return new DeleteTicket(LocalDateTime.now().toString(), id, owner, filename, fileSize);
  }
}
