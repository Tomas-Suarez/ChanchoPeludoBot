package com.chanchopeludo.ChanchoPeludoBot.repository;

import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.ServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayListRepository extends JpaRepository<PlayListEntity, Long> {

    Optional<PlayListEntity> findByNameAndServer(String name, ServerEntity server);

    @Query("SELECT p FROM PlayListEntity p WHERE p.name = :name AND (p.server = :server OR p.is_public = true)")
    List<PlayListEntity> searchByNameAndServerOrPublic(@Param("name") String name, @Param("server") ServerEntity server);
}
