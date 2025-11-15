package com.chanchopeludo.ChanchoPeludoBot.commands.playlist;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListItemEntity;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.GenericConstants.TITLE_ERROR_MISSING_ARGS;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_NOT_IN_VOICE_CHANNEL;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.PLAYLIST_USAGE_LOAD;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildErrorEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildSuccessEmbed;

@Component
public class PlayListLoadCommand implements Command {

    private final MusicService musicService;
    private final PlayListService playListService;

    public PlayListLoadCommand(MusicService musicService, PlayListService playListService) {
        this.musicService = musicService;
        this.playListService = playListService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("playlist-load", "Reproduce las canciones de la playlist.")
                .addOption(OptionType.STRING, "nombre", "El nombre de tu playlist.", true);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {

        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();
        if (member == null || voiceState == null || !voiceState.inAudioChannel()) {
            event.reply(MSG_NOT_IN_VOICE_CHANNEL).setEphemeral(true).queue();
            return;
        }

        AudioChannel voiceChannel = voiceState.getChannel();
        long voiceChannelId = voiceChannel.getIdLong();
        long textChannelId = event.getChannel().getIdLong();
        long guildId = event.getGuild().getIdLong();
        String playlistName = event.getOption("nombre").getAsString();

        try{
            PlayListEntity playlist = loadAndPlay(guildId, voiceChannelId, textChannelId, playlistName);

            MessageEmbed embed = buildSuccessEmbed(
                    "Playlist Cargada",
                    "Cargando **" + playlist.getItems().size() + "** canciones de la playlist **" + playlist.getName() + "**."
            );
            event.replyEmbeds(embed).queue();
        }catch (Exception e){
            MessageEmbed embed = buildErrorEmbed("Error al Cargar", e.getMessage());
            event.replyEmbeds(embed).setEphemeral(true).queue();
        }
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();
        if (member == null || voiceState == null || !voiceState.inAudioChannel()) {
            event.getChannel().sendMessage(MSG_NOT_IN_VOICE_CHANNEL).queue();
            return;
        }

        if (args.isEmpty()) {
            MessageEmbed embed = buildErrorEmbed(TITLE_ERROR_MISSING_ARGS, PLAYLIST_USAGE_LOAD);
            event.getChannel().sendMessageEmbeds(embed).queue();
            return;
        }

        AudioChannel voiceChannel = voiceState.getChannel();
        long guildId = event.getGuild().getIdLong();
        long voiceChannelId = voiceChannel.getIdLong();
        long textChannelId = event.getChannel().getIdLong();
        String playlistName = String.join(" ", args);

        try {
            PlayListEntity playlist = loadAndPlay(guildId, voiceChannelId, textChannelId, playlistName);

            MessageEmbed embed = buildSuccessEmbed(
                    "Playlist Cargada",
                    "Cargando **" + playlist.getItems().size() + "** canciones de la playlist **" + playlist.getName() + "**."
            );
            event.getChannel().sendMessageEmbeds(embed).queue();

        } catch (Exception e) {
            MessageEmbed embed = buildErrorEmbed("Error al Cargar", e.getMessage());
            event.getChannel().sendMessageEmbeds(embed).queue();
        }
    }

    private PlayListEntity loadAndPlay(long guildId, long voiceChannelId, long textChannelId, String playlistName) throws Exception {
        PlayListEntity playlist = playListService.loadPlayList(playlistName, String.valueOf(guildId));

        for (PlayListItemEntity item : playlist.getItems()) {
            musicService.loadAndPlay(guildId, voiceChannelId, textChannelId, item.getTrack_Identifier());
        }

        return playlist;
    }

    @Override
    public String getName() {
        return "playlist-load";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("playlist-load", "pl-load");
    }
}
