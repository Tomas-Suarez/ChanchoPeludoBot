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
    void loadAndPlayFromMessage(MessageReceivedEvent event, String trackUrl);

}
