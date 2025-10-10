package com.chanchopeludo.ChanchoPeludoBot.commands.imp;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_NOT_IN_VOICE_CHANNEL;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_PLAY_USAGE;

@Component
public class PlayCommandImp implements Command {
    private final MusicService musicService;

    public PlayCommandImp(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        if(args.isEmpty()){
            event.getChannel().sendMessage(MSG_PLAY_USAGE).queue();
        }

        AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
        if(userChannel == null){
            event.getChannel().sendMessage(MSG_NOT_IN_VOICE_CHANNEL).queue();
            return;
        }

        String url = String.join(" ", args);
        musicService.loadAndPlayFromMessage(event, url);
    }

    @Override
    public String getName() {
        return "play";
    }
}
