package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.dto.SpotifyTrack;
import com.chanchopeludo.ChanchoPeludoBot.service.SpotifyService;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.SpotifyConstants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SpotifyServiceImp implements SpotifyService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyServiceImp.class);

    private static final Pattern SPOTIFY_URL_PATTERN = Pattern.compile("open\\.spotify\\.com/track/([a-zA-Z0-9]+)");

    private final SpotifyApi spotifyApi;

    public SpotifyServiceImp(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    @Override
    public Optional<SpotifyTrack> getTrackFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = SPOTIFY_URL_PATTERN.matcher(url);
        if (!matcher.find()) {
            logger.warn(URL_NO_ID_WARN, url);
            return Optional.empty();
        }

        String trackId = matcher.group(1);

        try {
            logger.info(SPOTIFY_SEARCH_INFO, trackId);
            Track track = spotifyApi.getTrack(trackId).build().execute();

            if (track == null) {
                logger.warn(SPOTIFY_NO_RESULT_WARN, trackId);
                return Optional.empty();
            }

            String trackName = track.getName();
            String artistName = (track.getArtists().length > 0)
                    ? track.getArtists()[0].getName()
                    : UNKNOWN_ARTIST;

            logger.info(SPOTIFY_TRACK_FOUND_INFO, trackName, artistName);

            SpotifyTrack spotifyTrack = new SpotifyTrack(trackName, artistName);
            return Optional.of(spotifyTrack);

        } catch (Exception e) {
            logger.error(API_ERROR, trackId, e);
            return Optional.empty();
        }
    }
}