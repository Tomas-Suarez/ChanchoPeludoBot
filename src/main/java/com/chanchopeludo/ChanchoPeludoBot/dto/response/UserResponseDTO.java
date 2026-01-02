package com.chanchopeludo.ChanchoPeludoBot.dto.response;

public record UserResponseDTO(
        String userId,
        String username,
        String serverId,
        String serverName,
        int level,
        long xp
) {}