package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListItemEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.ServerEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.UserEntity;
import com.chanchopeludo.ChanchoPeludoBot.repository.PlayListItemRepository;
import com.chanchopeludo.ChanchoPeludoBot.repository.PlayListRepository;
import com.chanchopeludo.ChanchoPeludoBot.repository.ServerRepository;
import com.chanchopeludo.ChanchoPeludoBot.repository.UserRepository;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public void createPlayList(String playlistName, String guildId, String creatorId) {
        ServerEntity server = serverRepository.findById(guildId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el servidor!"));

        UserEntity creator = userRepository.findById(creatorId)
                .orElseThrow(()-> new EntityNotFoundException("No se encontró el usuario creador!"));

        //TODO: Implementar una excepción personalizada mas adelante
        if (playListRepository.existsByNameIgnoreCaseAndServerAndCreator(playlistName, server, creator)) {
            throw new IllegalArgumentException("Ya tienes una playlist llamada '**" + playlistName + "**'.");
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
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el servidor!"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        PlayListEntity playlist = playListRepository.findByNameAndServerAndCreator(playlistName, server, user)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la playlist '" + playlistName + "'."));

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
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el servidor!"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario!"));

        PlayListEntity playlist = playListRepository.findByNameAndServerAndCreator(playlistName, server, user)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la playlist '" + playlistName + "'."));

        playListRepository.delete(playlist);

    }

    @Override
    public void removeTrack(String playlistName, int trackOrder, String guildId) {


    }

    @Override
    public List<PlayListItemEntity> listPlayLists() {
        return List.of();
    }

    @Override
    @Transactional
    public List<PlayListEntity> viewPlayList(String playlistName, String guildId, String userId) {
        ServerEntity server = serverRepository.findById(guildId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el servidor!"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario!"));

        List<PlayListEntity> playlists = playListRepository.searchForLoad(playlistName, server, user);

        if (playlists.isEmpty()) {
            throw new EntityNotFoundException("No encontré ninguna playlist llamada '" + playlistName + "' (ni tuya ni pública).");
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
                .orElseThrow(() -> new EntityNotFoundException("Playlist no encontrada con ID: " + playlistId));

        if (!playlist.is_public() && !playlist.getCreator().getIdUser().equals(requesterId)) {
            throw new RuntimeException("Esta playlist es privada.");
        }

        playlist.getItems().size();
        playlist.getCreator().getUsername();
        playlist.getServer().getGuild_name();

        if (playlist.getItems().isEmpty()) {
            throw new RuntimeException("La playlist está vacía.");
        }

        return playlist;
    }

    @Override
    @Transactional
    public void renamePlayList(String oldPlayListName, String newPlayListName, String guildId, String userId) {

        if (oldPlayListName.equalsIgnoreCase(newPlayListName)) {
            return;
        }

        PlayListEntity playList = playListRepository.findByNameAndServer_IdServerAndCreator_IdUser(oldPlayListName, guildId, userId)
                .orElseThrow(()-> new RuntimeException("No se encontro ninguna playlist con el nombre: " + oldPlayListName));

        if(playListRepository.existsByNameAndServer_IdServerAndCreator_IdUser(newPlayListName, guildId, userId)){
            throw new RuntimeException("Ya tienes una playlist con el mismo nombre en este servidor!");
        }

        playList.setName(newPlayListName);
        playListRepository.save(playList);

    }

    @Override
    @Transactional
    public void updateVisibility(String playListName, boolean isPublic, String guildId, String userId) {
        PlayListEntity playList = playListRepository.findByNameAndServer_IdServerAndCreator_IdUser(playListName, guildId, userId)
                .orElseThrow(()-> new RuntimeException("No se encontró tu playlist llamada: " + playListName));

        playList.set_public(isPublic);
        playListRepository.save(playList);
    }
}
