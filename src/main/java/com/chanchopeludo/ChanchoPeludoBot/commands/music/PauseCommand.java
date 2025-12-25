package com.chanchopeludo.ChanchoPeludoBot.commands.music;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_NOT_IN_VOICE_CHANNEL;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_PAUSE_MUSIC;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildErrorEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildSuccessEmbed;

@Component
public class PauseCommand implements Command {
    private final MusicService musicService;

    public PauseCommand(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash(getName(), "Pausa la reproducción de la canción");
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        if (event.getMember().getVoiceState().getChannel() == null) {
            event.replyEmbeds(buildErrorEmbed("Error", MSG_NOT_IN_VOICE_CHANNEL))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        musicService.pause(event.getGuild().getIdLong());

        event.replyEmbeds(buildSuccessEmbed("Pausado", MSG_PAUSE_MUSIC))
                .queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (event.getMember().getVoiceState().getChannel() == null) {
            event.getChannel()
                    .sendMessageEmbeds(buildErrorEmbed("Error", MSG_NOT_IN_VOICE_CHANNEL))
                    .queue();
            return;
        }

        musicService.pause(event.getGuild().getIdLong());

        event.getChannel()
                .sendMessageEmbeds(buildSuccessEmbed("Pausado", MSG_PAUSE_MUSIC))
                .queue();
    }

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("pause");
    }
}
