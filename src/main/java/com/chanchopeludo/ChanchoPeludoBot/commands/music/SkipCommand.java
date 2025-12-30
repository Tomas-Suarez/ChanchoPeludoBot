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
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_SKIP_MUSIC;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildSuccessEmbed;

@Component
public class SkipCommand implements Command {

    private final MusicService musicService;

    public SkipCommand(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash(getName(), "Saltea la canción actual");
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {

        if (event.getMember().getVoiceState().getChannel() == null) {
            event.reply(MSG_NOT_IN_VOICE_CHANNEL).setEphemeral(true).queue();
            return;
        }

        String skippedTrack = musicService.skipTrack(event.getGuild().getIdLong());

        event.replyEmbeds(buildSuccessEmbed("Canción Saltada", MSG_SKIP_MUSIC + " " + skippedTrack))
                .queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {

        if (event.getMember().getVoiceState().getChannel() == null) {
            event.getChannel().sendMessage(MSG_NOT_IN_VOICE_CHANNEL).queue();
            return;
        }

        String skippedTrack = musicService.skipTrack(event.getGuild().getIdLong());

        event.getChannel()
                .sendMessageEmbeds(buildSuccessEmbed("Canción Saltada", MSG_SKIP_MUSIC + " " + skippedTrack))
                .queue();
    }

    @Override
    public String getName(){
        return "skip";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("skip", "s");
    }
}
