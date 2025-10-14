package com.chanchopeludo.ChanchoPeludoBot.commands.handlers;

import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.SpotifyService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.*;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.SpotifyConstants.SPOTIFY_URL_DETECTED;

@Component
public class SpotifyPlayListHandler implements InputHandler{
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
        logger.info(SPOTIFY_URL_DETECTED, input);
        event.getChannel().sendMessage(MSG_SPOTIFY_PROCESSING).queue();

        spotifyService.getPlaylistFromUrlAsync(input).thenAccept(playlist -> {
            if (playlist.isEmpty()) {
                event.getChannel().sendMessage(MSG_SPOTIFY_FAILURE).queue();
                return;
            }
            String message = String.format(MSG_PLAYLIST_ADDED_COUNT, playlist.size());
            event.getChannel().sendMessage(message).queue();
            playlist.forEach(track -> musicService.loadAndPlayFromMessage(event, track.toYoutubeSearchQuery()));
        });
    }
}
