package com.chanchopeludo.ChanchoPeludoBot.commands;

import com.chanchopeludo.ChanchoPeludoBot.commands.handlers.InputHandler;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_NOT_IN_VOICE_CHANNEL;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_PLAY_USAGE;

@Component
public class PlayCommand implements Command {

    private final List<InputHandler> handlers;

    public PlayCommand(List<InputHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        if(args.isEmpty()){
            event.getChannel().sendMessage(MSG_PLAY_USAGE);
        }

        final AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
        if(userChannel == null){
            event.getChannel().sendMessage(MSG_NOT_IN_VOICE_CHANNEL).queue();
        }

        String input = String.join(" ", args);

        for (InputHandler handler : handlers) {
            if (handler.canHandle(input)) {
                handler.handle(event, input);
                return;
            }
        }
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList("play", "p");
    }
}