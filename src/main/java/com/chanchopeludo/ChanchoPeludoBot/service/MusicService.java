package com.chanchopeludo.ChanchoPeludoBot.service;

import com.chanchopeludo.ChanchoPeludoBot.dto.PlayResult;
import com.chanchopeludo.ChanchoPeludoBot.dto.QueueState;

import java.util.concurrent.CompletableFuture;

public interface MusicService {

    /**
     * Carga y reproduce una canción o playlist
     *
     * @param guildId        ID del servidor (Guild).
     * @param voiceChannelId ID del canal de voz al que conectarse.
     * @param trackUrl       La URL de la canción a reproducir.
     * @return Un CompletableFuture que nos retorna un PlayResult con el estado y mensaje
     */
    CompletableFuture<PlayResult> loadAndPlay(long guildId, long voiceChannelId, long textChannelId, String trackUrl);
    /**
     * Saltea la canción que se encuentra reproduciendo y comienza la siguiente en la cola.
     *
     * @param guildId ID del servidor (Guild).
     * @return Un PlayResult con el estado y mensaje
     */
    PlayResult skipTrack(long guildId);

    /**
     * Detiene la reproducción de la música por completo, limpia la cola de canciones y desconecta al bot del canal de voz
     *
     * @param guildId ID del servidor (Guild).
     * @return Un PlayResult con el estado y mensaje
     */
    PlayResult stop(long guildId);

    /**
     * Pausa la reproducción de la cancion actual.
     *
     * @param guildId ID del servidor (Guild).
     * @return Un PlayResult con el estado y mensaje
     */
    PlayResult pause(long guildId);

    /**
     * Reanuda la reproducción de la canción que estaba en pausa.
     *
     * @param guildId ID del servidor (Guild).
     * @return Un PlayResult con el estado y mensaje
     */
    PlayResult resume(long guildId);

    /**
     * Obtiene el estado completo de la cola (canción actual y lista de espera).
     *
     * @param guildId  ID del servidor (Guild).
     * @return Un objeto QueueState con toda la información de la cola.
     */
    QueueState getQueueState(long guildId);

    /**
     * Busca y añade una canción a la cola de forma silenciosa (sin enviar mensajes al canal).
     * procesar las canciones de una playlist en segundo plano sin generar spam.
     *
     * @param guildId  ID del servidor (Guild).
     * @param trackUrl La URL de la cancion.
     * @return Un CompletableFuture que nos retorna un PlayResult con el estado y mensaje
     */
    CompletableFuture<PlayResult> queueTrack(long guildId, long textChannelId, String trackUrl);
    /**
     * Inicia la reproducción de una canción de forma silenciosa (sin enviar mensajes al canal).
     * Si ya hay una canción en reproducción, la añade a la cola. Usado para la primera canción de una playlist.
     *
     * @param guildId        ID del servidor (Guild).
     * @param voiceChannelId ID del canal de voz al que conectarse.
     * @param trackUrl       La URL de la cancion.
     * @return Un CompletableFuture que nos retorna un PlayResult con el estado y mensaje
     */
    CompletableFuture<PlayResult> playTrackSilently(long guildId, long voiceChannelId, long textChannelId, String trackUrl);

    /**
     * Ajusta el volumen de reproducción del bot de música.
     *
     * @param guildId     ID del servidor (Guild).
     * @param valueVolume El nuevo nivel de volumen, expresado como un porcentaje (0 a 100).
     *                    Un valor de 100 representa el volumen máximo, mientras que 0 silencia la reproducción.
     */
    PlayResult volume(long guildId, int valueVolume);

    /**
     * Mezcla la cola de reproducción actual.
     *
     * @param guildId ID del servidor (Guild).
     * @return Un PlayResult con el estado y mensaje
     */
    PlayResult shuffle(long guildId);

}
