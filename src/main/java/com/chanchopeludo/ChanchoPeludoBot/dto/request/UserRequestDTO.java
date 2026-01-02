package com.chanchopeludo.ChanchoPeludoBot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record UserRequestDTO(
        @NotBlank(message = "El ID del usuario es obligatorio")
        String userId,

        @NotBlank(message = "El ID del servidor es obligatorio")
        String serverId,

        @PositiveOrZero(message = "La XP no puede ser negativa")
        Long xp,

        @PositiveOrZero
        Integer level
) {}
