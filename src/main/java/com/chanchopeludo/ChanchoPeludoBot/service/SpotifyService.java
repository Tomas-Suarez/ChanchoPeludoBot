package com.chanchopeludo.ChanchoPeludoBot.service;

import com.chanchopeludo.ChanchoPeludoBot.dto.SpotifyTrack;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SpotifyService {

    /**
     * Busca de forma asíncrona una única canción de Spotify a partir de su URL.
     * <p>
     * La operación se realiza en un hilo separado para no bloquear la ejecución principal.
     *
     * @param url La URL completa de la canción de Spotify a buscar (ej: "https://open.spotify.com/track/trackId").
     * @return Un {@code CompletableFuture} que, al completarse, contendrá un {@code Optional} con el {@code SpotifyTrack} si se encontró,
     * o un {@code Optional} vacío si la URL es inválida o la canción no existe.
     */
    CompletableFuture<Optional<SpotifyTrack>> getTrackFromUrlAsync(String url);

    /**
     * Obtiene de forma asíncrona todas las canciones de una playlist de Spotify a partir de su URL.
     * <p>
     * La operación se realiza en un hilo separado. Si ocurre un error durante la obtención de datos
     * o la playlist no se encuentra, me devovlera una lista vacia.
     *
     * @param url La URL completa de la playlist de Spotify (ej: "https://open.spotify.com/playlist/playlistId").
     * @return Un {@code CompletableFuture} que, al completarse, contendrá una {@code List} de {@code SpotifyTrack}
     * con todas las canciones de la playlist. La lista estará vacía si la URL es inválida,
     * la playlist no existe o no contiene canciones.
     */
    CompletableFuture<List<SpotifyTrack>> getPlaylistFromUrlAsync(String url);

}