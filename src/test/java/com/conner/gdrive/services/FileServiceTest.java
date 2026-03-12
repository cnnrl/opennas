package com.conner.gdrive.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import com.conner.gdrive.models.FileMetadata;
import com.conner.gdrive.repositories.FileMetadataRepository;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

  @Mock
  FileMetadataRepository repo;

  @Mock
  FileEncryptService fes;

  @InjectMocks
  FileService fileService;

  @Test
  void saveFile_withValidInput_shouldSucceed() throws Exception {
    doNothing().when(fes).encrypt(any(), any());
    MockMultipartFile file = new MockMultipartFile(
        "file", "image.jpg", "image/jpeg", new byte[100]);
    FileMetadata result = fileService.saveFile(file, "conner");
    verify(repo).save(any(FileMetadata.class));
    assertEquals("image.jpg", result.getFileName());
    assertEquals("conner", result.getOwner());
  }

  @Test
  void saveFile_withEmptyFile_shouldThrowException() throws IOException {
    MockMultipartFile file = new MockMultipartFile(
        "file", "image.jpg", "image/jpeg", new byte[0]);
    assertThrows(IllegalArgumentException.class, () -> {
      fileService.saveFile(file, "conner");
    });
    verify(repo, never()).save(any(FileMetadata.class));
  }

  @Test
  void saveFile_withBlankOwner_shouldThrowException() throws IOException {
    MockMultipartFile file = new MockMultipartFile(
        "file", "image.jpg", "image/jpeg", new byte[100]);
    assertThrows(IllegalArgumentException.class, () -> {
      fileService.saveFile(file, "");
    });
    verify(repo, never()).save(any(FileMetadata.class));
  }

  @Test
  void getFiles_shouldReturnOwnerFiles() {
    FileMetadata md = new FileMetadata("id1", "image.jpg", "image/jpeg", 100, "conner");
    when(repo.findByOwner("conner")).thenReturn(List.of(md));
    List<FileMetadata> result = fileService.getFiles("conner");
    assertEquals(1, result.size());
    assertEquals("conner", result.get(0).getOwner());
  }

  @Test
  void fetchFile_withWrongOwner_shouldThrowAccessDeniedException() {
    FileMetadata md = new FileMetadata("id1", "image.jpg", "image/jpeg", 100, "conner");
    when(repo.findById("id1")).thenReturn(Optional.of(md));
    assertThrows(AccessDeniedException.class, () -> {
      fileService.fetchFile("id1", "hacker");
    });
  }

  @Test
  void fetchFile_withMissingId_shouldThrowException() {
    when(repo.findById("fakeid")).thenReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> {
      fileService.fetchFile("fakeid", "conner");
    });
  }

  @Test
  void fetchFile_withBlankId_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> {
      fileService.fetchFile("", "conner");
    });
  }
}
