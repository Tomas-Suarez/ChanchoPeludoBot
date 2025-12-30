package com.chanchopeludo.ChanchoPeludoBot.commands.playlist;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.dto.SpotifyTrack;
import com.chanchopeludo.ChanchoPeludoBot.dto.VideoInfo;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.InvalidInputException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ResourceNotFoundException;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
import com.chanchopeludo.ChanchoPeludoBot.service.SpotifyService;
import com.chanchopeludo.ChanchoPeludoBot.service.VideoInfoService;
import jakarta.persistence.EntityNotFoundException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.GenericConstants.TITLE_ERROR_MISSING_ARGS;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.*;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildErrorEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildSuccessEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.ValidationHelper.*;

@Component
public class PlayListAddCommand implements Command {

    private final PlayListService playListService;
    private final SpotifyService spotifyService;
    private final VideoInfoService videoInfoService;

    public PlayListAddCommand(PlayListService playListService,
                              SpotifyService spotifyService,
                              VideoInfoService videoInfoService) {
        this.playListService = playListService;
        this.spotifyService = spotifyService;
        this.videoInfoService = videoInfoService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("playlist-add", "Añade una canción a una playlist.")
                .addOption(OptionType.STRING, "playlist", "El nombre de tu playlist.", true)
                .addOption(OptionType.STRING, "cancion", "El nombre o URL (YT o Spotify) de la canción.", true);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {

        String playlistName = event.getOption("playlist").getAsString();
        String trackQuery = event.getOption("cancion").getAsString();
        String serverId = event.getGuild().getId();
        String userId = event.getUser().getId();

        MessageEmbed embed = handleAddTrack(serverId, playlistName, trackQuery, userId);

        event.replyEmbeds(embed).queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {

        if (args.size() < 2) {
            MessageEmbed embed = buildErrorEmbed(TITLE_ERROR_MISSING_ARGS, PLAYLIST_USAGE_ADD);
            event.getChannel().sendMessageEmbeds(embed).queue();
            return;
        }

        String playlistName = args.get(0);
        String trackQuery = args.subList(1, args.size())
                .stream()
                .collect(Collectors.joining(" "));
        String serverId = event.getGuild().getId();
        String userId = event.getAuthor().getId();

        MessageEmbed embed = handleAddTrack(serverId, playlistName, trackQuery, userId);

        event.getChannel().sendMessageEmbeds(embed).queue();
    }

    private MessageEmbed handleAddTrack(String serverId, String playlistName, String trackQuery, String userId) {
        String title;
        String trackIdentifier;

        try {
            if (isSpotifyTrack(trackQuery)) {
                SpotifyTrack track = spotifyService.getTrackFromUrlAsync(trackQuery).join();

                title = track.name();
                trackIdentifier = track.toYoutubeSearchQuery();

            } else if (isSpotifyPlaylist(trackQuery)) {
                throw new InvalidInputException("No puedes añadir una playlist de Spotify entera. Añade las canciones una por una.");

            } else if (isYoutubeUrl(trackQuery)) {
                VideoInfo info = videoInfoService.getVideoInfo(trackQuery).join();

                if (info == null || info.title() == null) {
                    throw new ResourceNotFoundException("No se pudo obtener la información del video de YouTube.");
                }
                title = info.title();
                trackIdentifier = trackQuery;

            } else {
                title = trackQuery;
                trackIdentifier = "ytsearch:" + trackQuery;
            }

            playListService.addTrackToPlayList(playlistName, serverId, userId, title, trackIdentifier);

            return buildSuccessEmbed(
                    TITLE_TRACK_ADDED,
                    String.format(DESC_TRACK_ADDED, title, playlistName)
            );
        } catch (Exception e) {
            Throwable cause = e;
            if (e instanceof CompletionException && e.getCause() != null) {
                cause = e.getCause();
            }
            return buildErrorEmbed(TITLE_ERROR_PLAYLIST_ADD, cause.getMessage());
        }
    }

    @Override
    public String getName() {
        return "playlist-add";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("playlist-add", "pl-add");
    }
}