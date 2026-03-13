package com.conner.gdrive.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conner.gdrive.models.FileMetadata;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {

  List<FileMetadata> findByOwner(String owner);

}
