package io.github.cnnrl.opennas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.cnnrl.opennas.models.SongMetadata;

public interface SongRepository extends JpaRepository<SongMetadata, String> {

}
