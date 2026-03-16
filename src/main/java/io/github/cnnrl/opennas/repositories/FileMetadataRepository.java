package io.github.cnnrl.opennas.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.cnnrl.opennas.models.FileMetadata;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {

  List<FileMetadata> findByOwner(String owner);

}
