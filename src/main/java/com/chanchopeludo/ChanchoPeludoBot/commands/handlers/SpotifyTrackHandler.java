package com.chanchopeludo.ChanchoPeludoBot.commands.handlers;

import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.SpotifyService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_SPOTIFY_FAILURE;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_SPOTIFY_PROCESSING;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.SpotifyConstants.SPOTIFY_URL_DETECTED;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.SpotifyConstants.SPOTIFY_URL_TRANSLATED;

@Component
public class SpotifyTrackHandler implements InputHandler{

    private final MusicService musicService;
    private final SpotifyService spotifyService;
    private static final Logger logger = LoggerFactory.getLogger(SpotifyTrackHandler.class);

    public SpotifyTrackHandler(MusicService musicService, SpotifyService spotifyService) {
        this.musicService = musicService;
        this.spotifyService = spotifyService;
    }

    @Override
    public boolean canHandle(String input) {
        return input.contains("spotify.com") && input.contains("/track/");
    }

    @Override
    public void handle(MessageReceivedEvent event, String input) {
        logger.info(SPOTIFY_URL_DETECTED, input);
        event.getChannel().sendMessage(MSG_SPOTIFY_PROCESSING).queue();
        spotifyService.getTrackFromUrlAsync(input).thenAccept(trackOptional -> {
            trackOptional.ifPresentOrElse(
                    track -> {
                        String finalInput = track.toYoutubeSearchQuery();
                        logger.info(SPOTIFY_URL_TRANSLATED, finalInput);
                        musicService.loadAndPlayFromMessage(event, finalInput);
                    },
                    () -> event.getChannel().sendMessage(MSG_SPOTIFY_FAILURE).queue()
            );
        });
    }
}
