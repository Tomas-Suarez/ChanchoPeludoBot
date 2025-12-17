package com.chanchopeludo.ChanchoPeludoBot.service;

import com.chanchopeludo.ChanchoPeludoBot.model.UserServerStatsEntity;

import java.util.Optional;

public interface UserService {

    /**
     * Añade experiencia a un usuario en el servidor.
     *
     * @param userId   El ID del usuario de Discord
     * @param serverId El ID del servidor de Discord
     * @param xpToAdd  La cantidad de experiencia a añadir
     */
    void addExp(String userId, String username, String serverId, String serverName, long xpToAdd);

    /**
     * Obtener nivel y expereciencia de un usuario en el servidor.
     *
     * @param userId   El ID del usuario de Discord
     * @param serverId El ID del servidor de Discord
     */
    int getLevel(String userId, String serverId);

    /**
     * Obtener todos los datos de tu perfil del servidor.
     *
     * @param userId   El ID del usuario de Discord
     * @param serverId El ID del servidor de Discord
     */
    Optional<UserServerStatsEntity> getProfile(String userId, String serverId);

}
