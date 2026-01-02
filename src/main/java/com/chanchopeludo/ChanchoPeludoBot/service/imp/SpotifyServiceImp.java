package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.dto.internal.SpotifyTrack;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ExternalServiceException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.InvalidInputException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ResourceNotFoundException;
import com.chanchopeludo.ChanchoPeludoBot.service.SpotifyService;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.ResourceNames.SONG_SPOTIFY;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.SpotifyConstants.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class SpotifyServiceImp implements SpotifyService {

    private static final Pattern SPOTIFY_URL_PATTERN = Pattern.compile("track/(\\w+)");
    private static final Pattern SPOTIFY_PLAYLIST_PATTERN = Pattern.compile("playlist/(\\w+)");


    private final SpotifyApi spotifyApi;

    public SpotifyServiceImp(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    @Override
    public CompletableFuture<SpotifyTrack> getTrackFromUrlAsync(String url) {
        if (url == null || url.isBlank()) {
            return CompletableFuture.failedFuture(new InvalidInputException("La URL de spotify no puede estar vaciá."));
        }

        return CompletableFuture.supplyAsync(() -> {
            Matcher matcher = SPOTIFY_URL_PATTERN.matcher(url);
            if (!matcher.find()) {
                log.warn("No se pudo encontrar un ID de track de Spotify en la URL: {}", url);
                throw new InvalidInputException(URL_INVALID_TRACK_ID);
            }
            String trackId = matcher.group(1);

            try {
                log.info("Buscando información en Spotify para el track ID: {}", trackId);
                Track track = spotifyApi.getTrack(trackId).build().execute();

                if (track == null) {
                    throw new ResourceNotFoundException(SONG_SPOTIFY, trackId);
                }

                String trackName = track.getName();
                String artistName = Stream.of(track.getArtists())
                        .findFirst()
                        .map(ArtistSimplified::getName)
                        .orElse(UNKNOWN_ARTIST);

                log.info("Track encontrado: '{}' por '{}", trackName, artistName);

                return new SpotifyTrack(trackName, artistName);

            } catch (ResourceNotFoundException | InvalidInputException e) {
                throw e;
            } catch (Exception e) {
                log.error("Fallo Spotify", e);
                throw new ExternalServiceException("Spotify", "No se pudo conectar con la API");
            }
        });
    }

    @Override
    public CompletableFuture<List<SpotifyTrack>> getPlaylistFromUrlAsync(String url) {
        if (url == null || url.isBlank()) {
            return CompletableFuture.failedFuture(new InvalidInputException("La URL de la playlist no puede estar vacía."));
        }

        return CompletableFuture.supplyAsync(() -> {
            Matcher matcher = SPOTIFY_PLAYLIST_PATTERN.matcher(url);
            if (!matcher.find()) {
                throw new InvalidInputException(URL_INVALID_TRACK_ID);
            }

            String playlistId = matcher.group(1);

            try {
                GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId)
                        .build();

                Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsItemsRequest.execute();

                if (playlistTrackPaging == null || playlistTrackPaging.getItems() == null) {
                    throw new ResourceNotFoundException("Playlist de Spotify", playlistId);
                }

                return Arrays.stream(playlistTrackPaging.getItems())
                        .map(playlistTrack -> (Track) playlistTrack.getTrack())
                        .map(track -> {
                            String artistName = Stream.of(track.getArtists())
                                    .findFirst()
                                    .map(ArtistSimplified::getName)
                                    .orElse(UNKNOWN_ARTIST);
                            return new SpotifyTrack(track.getName(), artistName);
                        })
                        .collect(Collectors.toList());

            } catch (ResourceNotFoundException e) {
                throw e;
            } catch (Exception e) {
                log.error("Fallo Spotify Playlist", e);
                throw new ExternalServiceException("Spotify", "No se pudo obtener la playlist.");
            }
        });
    }
}