package com.chanchopeludo.ChanchoPeludoBot.commands;

import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_NOT_IN_VOICE_CHANNEL;

@Component
public class ResumeCommand implements Command {
    private final MusicService musicService;

    public ResumeCommand(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        if (event.getMember() == null || event.getMember().getVoiceState() == null || !event.getMember().getVoiceState().inAudioChannel()) {
            event.getChannel().sendMessage(MSG_NOT_IN_VOICE_CHANNEL).queue();
            return;
        }
        musicService.resume(event);
    }

    @Override
    public String getName() {
        return "resume";
    }
}
