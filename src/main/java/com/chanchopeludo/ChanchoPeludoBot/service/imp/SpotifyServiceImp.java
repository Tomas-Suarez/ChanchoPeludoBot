package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.dto.SpotifyTrack;
import com.chanchopeludo.ChanchoPeludoBot.service.SpotifyService;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.SpotifyConstants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SpotifyServiceImp implements SpotifyService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyServiceImp.class);

    private static final Pattern SPOTIFY_URL_PATTERN = Pattern.compile("track/(\\w+)");
    private static final Pattern SPOTIFY_PLAYLIST_PATTERN = Pattern.compile("playlist/(\\w+)");


    private final SpotifyApi spotifyApi;

    public SpotifyServiceImp(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    @Override
    public CompletableFuture<Optional<SpotifyTrack>> getTrackFromUrlAsync(String url){
        if (url == null || url.isBlank()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return CompletableFuture.supplyAsync(()->{
            Matcher matcher = SPOTIFY_URL_PATTERN.matcher(url);
            if (!matcher.find()) {
                logger.warn("No se pudo encontrar un ID de track de Spotify en la URL: {}", url);
                throw new IllegalStateException(URL_INVALID_TRACK_ID);
            }
            String trackId = matcher.group(1);

            try{
                logger.info("Buscando información en Spotify para el track ID: {}", trackId);
                Track track = spotifyApi.getTrack(trackId).build().execute();

                if (track == null) {
                    return Optional.<SpotifyTrack>empty();
                }

                String trackName = track.getName();
                String artistName = Stream.of(track.getArtists())
                        .findFirst()
                        .map(ArtistSimplified::getName)
                        .orElse(UNKNOWN_ARTIST);

                logger.info("Track encontrado: '{}' por '{}", trackName, artistName);

                return Optional.of(new SpotifyTrack(trackName, artistName));

            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }).exceptionally(ex -> {
            logger.error("Error al procesar la URL de Spotify de forma asíncrona: ", ex);
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<List<SpotifyTrack>> getPlaylistFromUrlAsync(String url) {
        if (url == null || url.isBlank()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return CompletableFuture.supplyAsync(()->{
            Matcher matcher = SPOTIFY_PLAYLIST_PATTERN.matcher(url);
            if(!matcher.find()){
                throw new IllegalArgumentException(URL_INVALID_TRACK_ID);
            }

            String playlistId = matcher.group(1);

            try{
                GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId)
                        .build();

                Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsItemsRequest.execute();

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

            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }).exceptionally(ex->{
            logger.error("Error al procesar la URL de Spotify de forma asíncrona", ex);
            return Collections.emptyList();
        });
    }
}