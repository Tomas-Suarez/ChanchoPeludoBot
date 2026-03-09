package com.chanchopeludo.ChanchoPeludoBot.commands.playlist;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ExternalServiceException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.InvalidInputException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ResourceNotFoundException;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.GenericConstants.TITLE_ERROR_MISSING_ARGS;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.*;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildErrorEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildSuccessEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.ValidationHelper.*;

@Component
public class PlayListAddCommand implements Command {

    private final PlayListService playListService;
    private final LavalinkClient lavalinkClient;

    public PlayListAddCommand(PlayListService playListService, LavalinkClient lavalinkClient) {
        this.playListService = playListService;
        this.lavalinkClient = lavalinkClient;
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
        try {
            if (isSpotifyPlaylist(trackQuery)) {
                throw new InvalidInputException("No puedes añadir una playlist de Spotify entera. Añade las canciones una por una.");
            }

            String query = trackQuery;
            if (!isYoutubeUrl(trackQuery) && !isSpotifyTrack(trackQuery) && !trackQuery.startsWith("http")) {
                query = "ytsearch:" + trackQuery;
            }

            LavalinkLoadResult result = lavalinkClient.getOrCreateLink(Long.parseLong(serverId))
                    .loadItem(query)
                    .block();

            String title;
            String trackIdentifier;

            if (result instanceof TrackLoaded loaded) {
                title = loaded.getTrack().getInfo().getTitle();
                trackIdentifier = loaded.getTrack().getInfo().getUri();

            } else if (result instanceof PlaylistLoaded playlist) {
                throw new InvalidInputException("No puedes añadir una playlist entera a tu playlist personalizada. Añade las canciones una por una.");

            } else if (result instanceof SearchResult search) {
                if (search.getTracks().isEmpty()) {
                    throw new ResourceNotFoundException("No se encontraron coincidencias para: " + trackQuery);
                }
                Track first = search.getTracks().get(0);
                title = first.getInfo().getTitle();
                trackIdentifier = first.getInfo().getUri();

            } else if (result instanceof NoMatches) {
                throw new ResourceNotFoundException("No se encontraron resultados para tu búsqueda.");

            } else if (result instanceof LoadFailed failed) {
                throw new ExternalServiceException("Lavalink", failed.getException().getMessage());

            } else {
                throw new InvalidInputException("No se pudo resolver la canción.");
            }

            playListService.addTrackToPlayList(playlistName, serverId, userId, title, trackIdentifier);

            return buildSuccessEmbed(
                    TITLE_TRACK_ADDED,
                    String.format(DESC_TRACK_ADDED, title, playlistName)
            );

        } catch (Exception e) {
            return buildErrorEmbed(TITLE_ERROR_PLAYLIST_ADD, e.getMessage());
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