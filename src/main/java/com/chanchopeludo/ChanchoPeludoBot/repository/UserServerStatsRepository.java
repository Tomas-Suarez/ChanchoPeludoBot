package com.chanchopeludo.ChanchoPeludoBot.repository;

import com.chanchopeludo.ChanchoPeludoBot.model.UserServerStatsEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.UserServerStatsId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserServerStatsRepository extends JpaRepository<UserServerStatsEntity, UserServerStatsId> {
}
