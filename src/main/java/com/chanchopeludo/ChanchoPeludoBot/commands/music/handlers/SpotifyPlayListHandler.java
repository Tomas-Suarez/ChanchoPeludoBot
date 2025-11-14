package com.chanchopeludo.ChanchoPeludoBot.commands.music.handlers;

import com.chanchopeludo.ChanchoPeludoBot.dto.PlayResult;
import com.chanchopeludo.ChanchoPeludoBot.dto.SpotifyTrack;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.SpotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.function.Consumer;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.*;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.ValidationHelper.isSpotifyPlaylist;

@Component
public class SpotifyPlayListHandler implements InputHandler {
    private final MusicService musicService;
    private final SpotifyService spotifyService;
    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlayListHandler.class);

    public SpotifyPlayListHandler(MusicService musicService, SpotifyService spotifyService) {
        this.musicService = musicService;
        this.spotifyService = spotifyService;
    }

    @Override
    public boolean canHandle(String input) {
        return isSpotifyPlaylist(input);
    }

    @Override
    public void handle(long guildId, long voiceChannelId, long textChannelId, String input, Consumer<PlayResult> reply) {
        logger.info("Procesando URL de playlist de Spotify para el servidor '{}': {}", guildId, input);

        spotifyService.getPlaylistFromUrlAsync(input).thenAccept(tracks -> {
            if (tracks == null || tracks.isEmpty()) {
                logger.warn("La playlist de Spotify resultó vacía o nula para la URL: {}", input);
                reply.accept(new PlayResult(false, MSG_SPOTIFY_FAILURE));
                return;
            }

            logger.info("Servidor '{}': Playlist de Spotify con {} canciones recibidas.", guildId, tracks.size());
            SpotifyTrack firstTrack = tracks.get(0);
            String firstTrackQuery = firstTrack.toYoutubeSearchQuery();

            musicService.playTrackSilently(guildId, voiceChannelId, textChannelId, firstTrackQuery)
                    .thenAccept(playResult -> {

                        if (!playResult.success()) {
                            logger.warn("Falló la reproducción silenciosa de la primera canción de la playlist.");
                        }

                        for (int i = 1; i < tracks.size(); i++) {
                            SpotifyTrack track = tracks.get(i);
                            String trackQuery = track.toYoutubeSearchQuery();
                            musicService.queueTrack(guildId, textChannelId, trackQuery);
                        }

                        reply.accept(new PlayResult(true, String.format(MSG_PLAYLIST_ADDED_COUNT, tracks.size())));
                    });

        }).exceptionally(ex -> {
            logger.error("Ocurrió una excepción al obtener la playlist de Spotify para la URL: {}", input, ex);
            reply.accept(new PlayResult(false, "Error al procesar la playlist de Spotify."));
            return null;
        });
    }
}