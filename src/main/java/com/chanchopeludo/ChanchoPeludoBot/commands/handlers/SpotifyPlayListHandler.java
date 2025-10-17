package com.chanchopeludo.ChanchoPeludoBot.commands.handlers;

import com.chanchopeludo.ChanchoPeludoBot.dto.SpotifyTrack;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.SpotifyService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.*;

@Component
public class SpotifyPlayListHandler implements InputHandler {
    private final MusicService musicService;
    private final SpotifyService spotifyService;
    private static final Logger logger = LoggerFactory.getLogger(SpotifyTrackHandler.class);

    public SpotifyPlayListHandler(MusicService musicService, SpotifyService spotifyService) {
        this.musicService = musicService;
        this.spotifyService = spotifyService;
    }

    @Override
    public boolean canHandle(String input) {
        return input.contains("spotify.com") && input.contains("/playlist/");
    }

    @Override
    public void handle(MessageReceivedEvent event, String input) {
        logger.info("Procesando URL de playlist de Spotify para el servidor '{}': {}", event.getGuild().getId(), input);
        spotifyService.getPlaylistFromUrlAsync(input).thenAccept(tracks -> {
            if (tracks == null || tracks.isEmpty()) {
                logger.warn("La playlist de Spotify resultó vacía o nula para la URL: {}", input);
                event.getChannel().sendMessage(MSG_SPOTIFY_FAILURE).queue();
                return;
            }

            logger.info("Servidor '{}': Playlist de Spotify con {} canciones recibidas.", event.getGuild().getId(), tracks.size());
            SpotifyTrack firstTrack = tracks.get(0);
            String firstTrackQuery = firstTrack.toYoutubeSearchQuery();
            musicService.playTrackSilently(event, firstTrackQuery);

            for (int i = 1; i < tracks.size(); i++) {
                SpotifyTrack track = tracks.get(i);
                String trackQuery = track.toYoutubeSearchQuery();
                musicService.queueTrack(event.getGuild(), trackQuery);
            }

            event.getChannel().sendMessage(String.format(MSG_PLAYLIST_ADDED_COUNT, tracks.size())).queue();
        }).exceptionally(ex -> {
            logger.error("Ocurrió una excepción al obtener la playlist de Spotify para la URL: {}", input, ex);
            return null;
        });
    }
}
