package com.chanchopeludo.ChanchoPeludoBot.commands.imp;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class QueueCommandImp implements Command {

    private final MusicService musicService;

    public QueueCommandImp(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        musicService.showQueue(event);
    }

    @Override
    public String getName() {
        return "queue";
    }
}