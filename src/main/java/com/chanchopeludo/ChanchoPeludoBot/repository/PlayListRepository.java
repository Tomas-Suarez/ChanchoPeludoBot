package com.chanchopeludo.ChanchoPeludoBot.repository;

import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.ServerEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayListRepository extends JpaRepository<PlayListEntity, Long> {

    boolean existsByNameIgnoreCaseAndServerAndCreator(String name, ServerEntity server, UserEntity creator);

    Optional<PlayListEntity> findByNameAndServer(String name, ServerEntity server);

    Optional<PlayListEntity> findByNameAndServerAndCreator(String name, ServerEntity server, UserEntity creator);

    @Query("SELECT p FROM PlayListEntity p WHERE p.name = :name AND (p.server = :server OR p.is_public = true)")
    List<PlayListEntity> searchByNameAndServerOrPublic(@Param("name") String name, @Param("server") ServerEntity server);

    @Query("SELECT p FROM PlayListEntity p WHERE " +
            "LOWER(p.name) = LOWER(:name) AND " +
            "( " +
            "  (p.server = :server AND p.creator = :creator) OR " +
            "  (p.is_public = true) " +
            ") " +
            "ORDER BY CASE WHEN p.creator = :creator THEN 0 ELSE 1 END")
    List<PlayListEntity> searchForLoad(@Param("name") String name,
                                       @Param("server") ServerEntity server,
                                       @Param("creator") UserEntity creator);
}
