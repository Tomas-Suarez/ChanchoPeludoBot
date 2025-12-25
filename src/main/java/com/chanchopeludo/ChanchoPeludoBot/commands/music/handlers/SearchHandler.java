package com.chanchopeludo.ChanchoPeludoBot.commands.music.handlers;

import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.ValidationHelper.isUrl;

@Component
public class SearchHandler implements InputHandler {
    private final MusicService musicService;

    public SearchHandler(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public boolean canHandle(String input) {
        return !isUrl(input);
    }

    @Override
    public CompletableFuture<String> handle(long guildId, long voiceChannelId, long textChannelId, String input) {
        String search = "ytsearch:" + input;

        return musicService.loadAndPlay(guildId, voiceChannelId, textChannelId, search);
    }
}