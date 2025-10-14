package com.chanchopeludo.ChanchoPeludoBot.commands.imp;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.dto.SpotifyTrack;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.SpotifyService;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.*;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.SpotifyConstants.*;

@Component
public class PlayCommandImp implements Command {

    private static final Logger logger = LoggerFactory.getLogger(PlayCommandImp.class);

    private final MusicService musicService;
    private final SpotifyService spotifyService;

    public PlayCommandImp(MusicService musicService, SpotifyService spotifyService) {
        this.musicService = musicService;
        this.spotifyService = spotifyService;
    }

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        if (args.isEmpty()) {
            event.getChannel().sendMessage(MSG_PLAY_USAGE).queue();
            return;
        }

        final AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
        if (userChannel == null) {
            event.getChannel().sendMessage(MSG_NOT_IN_VOICE_CHANNEL).queue();
            return;
        }

        String input = String.join(" ", args);
        String finalInput = input;

        if (isUrl(input) && input.contains("spotify.com")) {
            logger.info(SPOTIFY_URL_DETECTED, input);
            event.getChannel().sendMessage(MSG_SPOTIFY_PROCESSING).queue();

            Optional<SpotifyTrack> trackOptional = spotifyService.getTrackFromUrl(input);

            if (trackOptional.isPresent()) {
                SpotifyTrack track = trackOptional.get();
                finalInput = track.toYoutubeSearchQuery();
                logger.info(SPOTIFY_URL_TRANSLATED, finalInput);
            } else {
                event.getChannel().sendMessage(MSG_SPOTIFY_FAILURE).queue();
                return;
            }
        } else if (!isUrl(input)) {
            finalInput = "ytsearch:" + input;
        }

        musicService.loadAndPlayFromMessage(event, finalInput);
    }

    private boolean isUrl(String url) {
        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "play";
    }
}