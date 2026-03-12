package com.conner.gdrive.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.io.Decoders;

@Service
public class FileEncryptService {
  private final SecretKeySpec key;
  private final SecureRandom rand = new SecureRandom();

  public FileEncryptService(@Value("${crypt.secret}") String secret) {
    key = new SecretKeySpec(Decoders.BASE64.decode(secret), "AES");
  }

  public void encrypt(InputStream in, OutputStream out) throws IOException, GeneralSecurityException {
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

    byte[] iv = new byte[12];
    rand.nextBytes(iv);
    out.write(iv);

    GCMParameterSpec params = new GCMParameterSpec(128, iv);
    cipher.init(Cipher.ENCRYPT_MODE, key, params);
    try (CipherOutputStream cos = new CipherOutputStream(out, cipher);) {
      in.transferTo(cos);
    }
  }

  public InputStream decrypt(InputStream in) throws IOException, GeneralSecurityException {
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

    byte[] iv = in.readNBytes(12);
    if (iv.length != 12)
      throw new IOException("Invalid Encrypted File!");
    GCMParameterSpec params = new GCMParameterSpec(128, iv);
    cipher.init(Cipher.DECRYPT_MODE, key, params);

    return new CipherInputStream(in, cipher);
  }
}
