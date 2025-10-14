package com.chanchopeludo.ChanchoPeludoBot.commands.handlers;

import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.ValidationHelper.isUrl;

@Component
public class YoutubeUrlHandler implements InputHandler{

    private final MusicService musicService;

    public YoutubeUrlHandler(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public boolean canHandle(String input) {
        return isUrl(input) && input.contains("youtube.com");
    }

    @Override
    public void handle(MessageReceivedEvent event, String input) {
        musicService.loadAndPlayFromMessage(event, input);

    }
}
