package com.chanchopeludo.ChanchoPeludoBot.commands;

import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_NOT_IN_VOICE_CHANNEL;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_INVALID_VALUE_VOLUME;

@Component
public class VolumeCommand implements Command {

    private final MusicService musicService;

    public VolumeCommand(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        if (event.getMember() == null || event.getMember().getVoiceState() == null || !event.getMember().getVoiceState().inAudioChannel()) {
            event.getChannel().sendMessage(MSG_NOT_IN_VOICE_CHANNEL).queue();
            return;
        }

        if (args.isEmpty()) {
            event.getChannel().sendMessage(MSG_INVALID_VALUE_VOLUME).queue();
            return;
        }

        try {
            int valueVolume = Integer.parseInt(args.get(0));
            musicService.volume(event, valueVolume);
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage(MSG_INVALID_VALUE_VOLUME).queue();
        }
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList("volume");
    }
}
