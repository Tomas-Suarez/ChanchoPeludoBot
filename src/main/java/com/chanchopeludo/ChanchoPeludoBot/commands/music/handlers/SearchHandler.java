package com.chanchopeludo.ChanchoPeludoBot.commands.music.handlers;

import com.chanchopeludo.ChanchoPeludoBot.dto.PlayResult;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

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
    public void handle(long guildId, long voiceChannelId, long textChannelId, String input, Consumer<PlayResult> response) {
        String search = "ytsearch:" + input;

        musicService.loadAndPlay(guildId, voiceChannelId, textChannelId, search)
                .thenAccept(response);
    }
}