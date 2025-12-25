package com.chanchopeludo.ChanchoPeludoBot.commands.music.handlers;

import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.SpotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.*;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.ValidationHelper.isSpotifyTrack;

@Component
public class SpotifyTrackHandler implements InputHandler {

    private final MusicService musicService;
    private final SpotifyService spotifyService;
    private static final Logger logger = LoggerFactory.getLogger(SpotifyTrackHandler.class);

    public SpotifyTrackHandler(MusicService musicService, SpotifyService spotifyService) {
        this.musicService = musicService;
        this.spotifyService = spotifyService;
    }

    @Override
    public boolean canHandle(String input) {
        return isSpotifyTrack(input);
    }

    @Override
    public CompletableFuture<String> handle(long guildId, long voiceChannelId, long textChannelId, String input) {
        logger.info("Procesando URL de track de Spotify para el servidor '{}': {}", guildId, input);

        return spotifyService.getTrackFromUrlAsync(input)
                .thenCompose(track -> {
                    String youtubeQuery = track.toYoutubeSearchQuery();
                    logger.info("Servidor '{}': URL de Spotify buscada: {}", guildId, youtubeQuery);

                    return musicService.loadAndPlay(guildId, voiceChannelId, textChannelId, youtubeQuery);

                });
    }
}