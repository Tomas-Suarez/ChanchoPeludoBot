package com.chanchopeludo.ChanchoPeludoBot.listeners;

import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.CommandConstants.MSG_SLASH_NO_VOICE;

public class MusicCommandListener extends ListenerAdapter {

    private final JDA jda;
    private final MusicService musicService;

    public MusicCommandListener(JDA jda, MusicService musicService) {
        this.jda = jda;
        this.musicService = musicService;
    }

    @PostConstruct
    public void register(){
        jda.addEventListener(this);
    }

    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(!event.getName().equals("play")) return;

        event.deferReply().queue();

        if (event.getMember() == null || event.getMember().getVoiceState() == null || !event.getMember().getVoiceState().inAudioChannel()) {
            event.getHook().sendMessage(MSG_SLASH_NO_VOICE).queue();
            return;
        }

        String url = event.getOption("url").getAsString();
        musicService.loadAndPlay(event, url);
    }
}
