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
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_RESUME_MUSIC;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildSuccessEmbed;

@Component
public class ResumeCommand implements Command {
    private final MusicService musicService;

    public ResumeCommand(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash(getName(), "Reanuda la canción que se encontraba en pausa");
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        if (event.getMember().getVoiceState().getChannel() == null) {
            event.reply(MSG_NOT_IN_VOICE_CHANNEL).setEphemeral(true).queue();
            return;
        }

        musicService.resume(event.getGuild().getIdLong());

        event.replyEmbeds(buildSuccessEmbed("Reanudado", MSG_RESUME_MUSIC)).queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (event.getMember().getVoiceState().getChannel() == null) {
            event.getChannel().sendMessage(MSG_NOT_IN_VOICE_CHANNEL).queue();
            return;
        }

        musicService.resume(event.getGuild().getIdLong());

        event.getChannel().sendMessageEmbeds(buildSuccessEmbed("Reanudado", MSG_RESUME_MUSIC)).queue();
    }

    @Override
    public String getName() {
        return "resume";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("resume");
    }
}
