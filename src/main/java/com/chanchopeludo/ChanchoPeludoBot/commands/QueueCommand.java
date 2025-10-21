package com.chanchopeludo.ChanchoPeludoBot.commands;

import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class QueueCommand implements Command {

    private final MusicService musicService;

    public QueueCommand(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        musicService.showQueue(event);
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList("queue");
    }
}