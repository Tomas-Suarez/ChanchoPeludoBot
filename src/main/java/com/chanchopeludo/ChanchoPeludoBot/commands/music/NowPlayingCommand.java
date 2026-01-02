package com.chanchopeludo.ChanchoPeludoBot.commands.music;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.dto.internal.AudioTrackInfo;
import com.chanchopeludo.ChanchoPeludoBot.dto.internal.QueueState;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_NOTHING_PLAYING;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildNowPlayingEmbed;

@Component
public class NowPlayingCommand implements Command {

    private final MusicService musicService;

    public NowPlayingCommand(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash(getName(), "Muestra la canción que está sonando en este momento");
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {

        QueueState state = musicService.getQueueState(event.getGuild().getIdLong());

        Optional<AudioTrackInfo> np = state.getNowPlaying();

        if (np.isPresent()) {
            AudioTrackInfo track = np.get();
            long position = state.nowPlayingPosition();

            MessageEmbed embed = buildNowPlayingEmbed(track, position);
            event.replyEmbeds(embed).queue();
        } else {
            event.reply(MSG_NOTHING_PLAYING).setEphemeral(true).queue();
        }
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        QueueState state = musicService.getQueueState(event.getGuild().getIdLong());

        Optional<AudioTrackInfo> np = state.getNowPlaying();

        if (np.isPresent()) {
            AudioTrackInfo track = np.get();
            long position = state.nowPlayingPosition();

            MessageEmbed embed = buildNowPlayingEmbed(track, position);
            event.getChannel().sendMessageEmbeds(embed).queue();
        } else {
            event.getChannel().sendMessage(MSG_NOTHING_PLAYING).queue();
        }
    }

    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("nowplaying", "np");
    }
}
