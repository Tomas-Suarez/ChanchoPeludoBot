package com.chanchopeludo.ChanchoPeludoBot.service;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface MusicService {

    /**
     * Carga y reproduce una canción o playlist desde una URL.
     * @param event El evento del comando que inició la acción.
     * @param trackUrl La URL de la canción a reproducir.
     */
    void loadAndPlay(SlashCommandInteractionEvent event, String trackUrl);

    /**
     * Carga y reproduce una canción desde una URL usando un evento de mensaje de texto.
     * @param event El evento del mensaje que inició la acción (ej: "c!play").
     * @param trackUrl La URL de la canción a reproducir.
     */
    void loadAndPlayFromMessage(MessageReceivedEvent event, String trackUrl);

    /**
     * Saltea la canción que se encuentra reproduciendo y comienza la siguiente en la cola.
     * @param event El evento del mensaje que inició la acción (ej: "c!skip").
     */
    void skipTrack(MessageReceivedEvent event);

    /**
     * Detiene la reproducción de la música por completo, limpia la cola de canciones y desconecta al bot del canal de voz
     * @param event El evento del mensaje que inició la acción (ej: "c!stop").
     */
    void stop(MessageReceivedEvent event);

    /**
     * Pausa la reproducción de la cancion actual.
     * @param event El evento del mensaje que inició la acción (ej: "c!pause").
     */
    void pause(MessageReceivedEvent event);

    /**
     * Reanuda la reproducción de la canción que estaba en pausa.
     * @param event El evento del mensaje que inició la acción (ej: "c!resume").
     */
    void resume(MessageReceivedEvent event);

    /**
     * Muestra la lista de reproducciones de canciones.
     * @param event El evento del mensaje que inició la acción (ej: "c!queue").
     */
    void showQueue(MessageReceivedEvent event);
}
