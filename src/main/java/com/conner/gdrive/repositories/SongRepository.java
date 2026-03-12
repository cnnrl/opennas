package com.conner.gdrive.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.conner.gdrive.models.SongMetadata;

public interface SongRepository extends JpaRepository<SongMetadata, String> {

}
