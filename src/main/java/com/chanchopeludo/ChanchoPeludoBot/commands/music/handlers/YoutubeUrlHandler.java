package com.chanchopeludo.ChanchoPeludoBot.commands.music.handlers;

import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.ValidationHelper.isYoutubeUrl;

@Component
public class YoutubeUrlHandler implements InputHandler{

    private final MusicService musicService;

    public YoutubeUrlHandler(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public boolean canHandle(String input) {
        return isYoutubeUrl(input);
    }

    @Override
    public CompletableFuture<String> handle(long guildId, long voiceChannelId, long textChannelId, String input) {
        return musicService.loadAndPlay(guildId, voiceChannelId, textChannelId, input);
    }
}