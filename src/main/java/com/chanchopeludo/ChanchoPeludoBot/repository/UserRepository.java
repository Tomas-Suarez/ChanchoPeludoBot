package com.chanchopeludo.ChanchoPeludoBot.repository;

import com.chanchopeludo.ChanchoPeludoBot.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
