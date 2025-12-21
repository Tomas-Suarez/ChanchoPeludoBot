package com.chanchopeludo.ChanchoPeludoBot.service;

import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;

import java.util.List;

public interface PlayListService {

    /**
     * Crea una nueva playlist vacía asociada a un usuario y un servidor.
     *
     * @param playlistName El nombre de la nueva playlist.
     * @param guildId      El ID del servidor.
     * @param userId       El ID del usuario.
     */
    void createPlayList(String playlistName, String guildId, String userId);

    /**
     * Se agrega una canción a una playlist ya existente
     *
     * @param playlistName    El nombre de la playlist existente.
     * @param guildId         El ID del servidor.
     * @param userId          El ID del usuario.
     * @param title           El título de la canción para mostrar
     * @param trackIdentifier La URL de la canción
     */
    void addTrackToPlayList(String playlistName, String guildId, String userId, String title, String trackIdentifier);

    /**
     * Elimina permanentemente una playlist completa.
     *
     * @param playlistName El nombre de la playlist a eliminar.
     * @param guildId      El ID del servidor.
     * @param userId       El ID del usuario (Dueño).
     */
    void deletePlayList(String playlistName, String guildId, String userId);

    /**
     * Elimina una canción específica de una playlist basándose en su posición.
     *
     * @param playlistName  El nombre de la playlist.
     * @param trackPosition La posición de la canción a borrar (índice basado en 1 visualmente).
     * @param guildId       El ID del servidor.
     * @param userId        El ID del usuario (Dueño)
     * @return El nombre (título) de la canción que fue eliminada.
     */
    String removeTrack(String playlistName, int trackPosition, String guildId, String userId);

    /**
     * Busca y muestra las canciones de una playlist específica.
     *
     * @param playlistName El nombre de la playlist.
     * @param guildId      El ID del servidor.
     * @param userId       El ID del usuario que solicita verla (Solo puede ver las playlists suyas y las publicas).
     * @return Lista de canciones de la playlist.
     */
    List<PlayListEntity> viewPlayList(String playlistName, String guildId, String userId);

    /**
     * Carga una playlist por su ID de la base de datos.
     *
     * @param playlistId  El ID de la playlist.
     * @param requesterId El ID del usuario que solicita cargarla.
     * @return La entidad de la playlist.
     */
    PlayListEntity loadPlayListById(Long playlistId, String requesterId);

    /**
     * Cambia el nombre de una playlist existente.
     *
     * @param oldPlayListName El nombre actual de la playlist.
     * @param newPlayListName El nuevo nombre deseado.
     * @param guildId         El ID del servidor.
     * @param userId          El ID del usuario (dueño).
     */
    void renamePlayList(String oldPlayListName, String newPlayListName, String guildId, String userId);

    /**
     * Actualiza la visibilidad de una playlist (Pública vs Privada).
     *
     * @param playListName El nombre de la playlist.
     * @param isPublic     {@code true} para hacerla pública (todos la ven), {@code false} para privada.
     * @param guidId       El ID del servidor.
     * @param userId       El ID del usuario (dueño).
     */
    void updateVisibility(String playListName, boolean isPublic, String guidId, String userId);

    /**
     * Obtiene una lista de todas las playlists marcadas como PÚBLICAS en un servidor.
     *
     * @param guildId El ID del servidor.
     * @return Una lista de entidades de playlist públicas.
     */
    List<PlayListEntity> getPublicPlayLists(String guildId);

    /**
     * Obtiene una lista de todas las playlists creadas por un usuario específico en un servidor.
     *
     * @param userId  El ID del usuario creador.
     * @param guildId El ID del servidor.
     * @return Una lista de playlist perteneciente al usuario.
     */
    List<PlayListEntity> getUserPlayLists(String userId, String guildId);
}
