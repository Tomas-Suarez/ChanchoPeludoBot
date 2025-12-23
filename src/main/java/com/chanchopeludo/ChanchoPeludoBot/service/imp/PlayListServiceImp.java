package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.exceptions.DuplicateResourceException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ForbiddenException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.InvalidInputException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ResourceNotFoundException;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListItemEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.ServerEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.UserEntity;
import com.chanchopeludo.ChanchoPeludoBot.repository.PlayListRepository;
import com.chanchopeludo.ChanchoPeludoBot.repository.ServerRepository;
import com.chanchopeludo.ChanchoPeludoBot.repository.UserRepository;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.exceptions.detailed.NotFoundException;

import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.ResourceNames.GUILD_DISCORD;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.ResourceNames.USER_CREATOR;

@Service
public class PlayListServiceImp implements PlayListService {

    private final PlayListRepository playListRepository;
    private final UserRepository userRepository;
    private final ServerRepository serverRepository;

    public PlayListServiceImp(PlayListRepository playListRepository, UserRepository userRepository, ServerRepository serverRepository) {
        this.playListRepository = playListRepository;
        this.userRepository = userRepository;
        this.serverRepository = serverRepository;
    }

    @Override
    @Transactional
    public void createPlayList(String playlistName, String guildId, String userId) {
        ServerEntity server = serverRepository.findById(guildId)
                .orElseThrow(() -> new ResourceNotFoundException(GUILD_DISCORD, guildId));

        UserEntity creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_CREATOR, userId));

        if (playListRepository.existsByNameIgnoreCaseAndServerAndCreator(playlistName, server, creator)) {
            throw new DuplicateResourceException("Playlist", playlistName);
        }

        PlayListEntity newPlayList = PlayListEntity.builder()
                .name(playlistName)
                .is_public(false)
                .server(server)
                .creator(creator)
                .build();

        playListRepository.save(newPlayList);
    }

    @Override
    @Transactional
    public void addTrackToPlayList(String playlistName, String guildId, String userId, String title, String trackIdentifier) {
        ServerEntity server = serverRepository.findById(guildId)
                .orElseThrow(() -> new ResourceNotFoundException(GUILD_DISCORD, guildId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        PlayListEntity playlist = playListRepository.findByNameAndServerAndCreator(playlistName, server, user)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", "nombre", playlistName));

        PlayListItemEntity newTrack = PlayListItemEntity.builder()
                .title(title)
                .track_Identifier(trackIdentifier)
                .playlist(playlist)
                .build();

        playlist.getItems().add(newTrack);
        playListRepository.save(playlist);
    }

    @Override
    public void deletePlayList(String playlistName, String guildId, String userId) {
        ServerEntity server = serverRepository.findById(guildId)
                .orElseThrow(() -> new ResourceNotFoundException(GUILD_DISCORD, guildId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        PlayListEntity playlist = playListRepository.findByNameAndServerAndCreator(playlistName, server, user)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", "nombre", playlistName));

        playListRepository.delete(playlist);

    }

    @Override
    @Transactional
    public String removeTrack(String playlistName, int trackPosition, String guildId, String userId) {

        PlayListEntity playlist = playListRepository.findByNameAndServer_IdServerAndCreator_IdUser(playlistName, guildId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", "nombre", playlistName));

        int index = trackPosition - 1;

        if (index < 0 || index >= playlist.getItems().size()) {
            throw new InvalidInputException(String.format(
                    "La posición %d no es válida. La playlist solo tiene %d canciones.",
                    trackPosition,
                    playlist.getItems().size()
            ));
        }

        PlayListItemEntity removedItem = playlist.getItems().remove(index);

        playListRepository.save(playlist);

        return removedItem.getTitle();
    }

    @Override
    @Transactional
    public List<PlayListEntity> viewPlayList(String playlistName, String guildId, String userId) {
        ServerEntity server = serverRepository.findById(guildId)
                .orElseThrow(() -> new ResourceNotFoundException(GUILD_DISCORD, guildId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        List<PlayListEntity> playlists = playListRepository.searchForLoad(playlistName, server, user);

        if (playlists.isEmpty()) {
            throw new ResourceNotFoundException("PlayList", "nombre", playlistName);
        }

        for (PlayListEntity pl : playlists) {
            pl.getItems().size();
            pl.getCreator().getUsername();
        }

        return playlists;
    }

    @Override
    @Transactional
    public PlayListEntity loadPlayListById(Long playlistId, String requesterId) {
        PlayListEntity playlist = playListRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("PlayList", String.valueOf(playlistId)));

        if (!playlist.is_public() && !playlist.getCreator().getIdUser().equals(requesterId)) {
            throw new ForbiddenException("Esta playlist es privada.");
        }

        playlist.getItems().size();
        playlist.getCreator().getUsername();
        playlist.getServer().getGuild_name();

        if (playlist.getItems().isEmpty()) {
            throw new InvalidInputException(String.format("La playlist '%s' está vacía. Agrega canciones antes de reproducirla.", playlist.getName()));
        }

        return playlist;
    }

    @Override
    @Transactional
    public void renamePlayList(String oldPlayListName, String newPlayListName, String guildId, String userId) {

        if (oldPlayListName.equalsIgnoreCase(newPlayListName)) {
            return;
        }

        PlayListEntity playlist = playListRepository.findByNameAndServer_IdServerAndCreator_IdUser(oldPlayListName, guildId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", "nombre", oldPlayListName));

        if (playListRepository.existsByNameAndServer_IdServerAndCreator_IdUser(newPlayListName, guildId, userId)) {
            throw new DuplicateResourceException("PlayList", newPlayListName);
        }

        playlist.setName(newPlayListName);
        playListRepository.save(playlist);

    }

    @Override
    @Transactional
    public void updateVisibility(String playListName, boolean isPublic, String guildId, String userId) {
        PlayListEntity playList = playListRepository.findByNameAndServer_IdServerAndCreator_IdUser(playListName, guildId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", "nombre", playListName));

        playList.set_public(isPublic);
        playListRepository.save(playList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayListEntity> getPublicPlayLists(String guildId) {
        List<PlayListEntity> playlists = playListRepository.findPublicByServer(guildId);

        if (playlists.isEmpty()) {
            throw new ResourceNotFoundException("Playlists públicas", guildId);
        }

        playlists.forEach(pl -> pl.getItems().size());

        return playlists;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayListEntity> getUserPlayLists(String userId, String guildId) {
        List<PlayListEntity> playlists = playListRepository.findByCreatorAndServer(userId, guildId);

        if (playlists.isEmpty()) {
            throw new ResourceNotFoundException("Tus Playlists", "usuario", userId);
        }

        playlists.forEach(pl -> pl.getItems().size());

        return playlists;
    }
}
