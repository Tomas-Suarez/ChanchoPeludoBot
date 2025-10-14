package com.chanchopeludo.ChanchoPeludoBot.commands.handlers;

import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.ValidationHelper.isUrl;

@Component
public class SearchHandler implements InputHandler{
    private final MusicService musicService;

    public SearchHandler(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public boolean canHandle(String input) {
        return !isUrl(input);
    }

    @Override
    public void handle(MessageReceivedEvent event, String input) {
        String search = "ytsearch:" + input;
        musicService.loadAndPlayFromMessage(event, search);
    }
}
