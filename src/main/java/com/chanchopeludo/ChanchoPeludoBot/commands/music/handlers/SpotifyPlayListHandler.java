package com.chanchopeludo.ChanchoPeludoBot.commands.music.handlers;

import com.chanchopeludo.ChanchoPeludoBot.dto.internal.SpotifyTrack;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ResourceNotFoundException;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.SpotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<String> handle(long guildId, long voiceChannelId, long textChannelId, String input) {
        logger.info("Procesando URL de playlist de Spotify para el servidor '{}': {}", guildId, input);

        return spotifyService.getPlaylistFromUrlAsync(input)
                .thenCompose(tracks -> {
                    if (tracks == null || tracks.isEmpty()) {
                        logger.warn("La playlist de Spotify resultó vacía o nula para la URL: {}", input);
                        throw new ResourceNotFoundException("La playlist de Spotify está vacía.");
                    }

                    logger.info("Servidor '{}': Playlist de Spotify con {} canciones recibidas.", guildId, tracks.size());
                    SpotifyTrack firstTrack = tracks.get(0);
                    String firstTrackQuery = firstTrack.toYoutubeSearchQuery();

                    return musicService.playTrackSilently(guildId, voiceChannelId, textChannelId, firstTrackQuery)
                            .thenApply(unused -> {
                                for (int i = 1; i < tracks.size(); i++) {
                                    SpotifyTrack track = tracks.get(i);
                                    musicService.queueTrack(guildId, textChannelId, track.toYoutubeSearchQuery());
                                }

                                return String.format(MSG_PLAYLIST_ADDED_COUNT, tracks.size());
                            });
                });
    }
}