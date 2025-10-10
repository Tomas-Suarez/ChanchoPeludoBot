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

}
