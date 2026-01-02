package com.chanchopeludo.ChanchoPeludoBot.controller;

import com.chanchopeludo.ChanchoPeludoBot.dto.response.UserResponseDTO;
import com.chanchopeludo.ChanchoPeludoBot.mappers.UserMapper;
import com.chanchopeludo.ChanchoPeludoBot.model.UserServerStatsEntity;
import com.chanchopeludo.ChanchoPeludoBot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/{userId}/servers/{serverId}")
    public ResponseEntity<UserResponseDTO> getUserProfile(@PathVariable String userId, @PathVariable String serverId){
        log.info("GET /users/{}/servers/{}", userId, serverId);

        UserServerStatsEntity entity = userService.getProfile(userId, serverId);

        UserResponseDTO response = userMapper.toResponseDTO(entity);

        return ResponseEntity.ok(response);
    }
}
