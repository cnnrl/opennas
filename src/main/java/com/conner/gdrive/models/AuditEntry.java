package com.conner.gdrive.models;

import java.time.LocalDateTime;

public record AuditEntry(String time, String ipaddr, String user, String action, String file, boolean auth) {
  public static AuditEntry of(String ipaddr, String user, String action, String file, boolean auth) {
    return new AuditEntry(LocalDateTime.now().toString(), ipaddr, user, action, file, auth);
  }
}
