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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayListServiceImp implements PlayListService {

    private final PlayListRepository playListRepository;
    private final PlayListItemRepository playListItemRepository;
    private final UserRepository userRepository;
    private final ServerRepository serverRepository;

    public PlayListServiceImp(PlayListRepository playListRepository, PlayListItemRepository playListItemRepository, UserRepository userRepository, ServerRepository serverRepository) {
        this.playListRepository = playListRepository;
        this.playListItemRepository = playListItemRepository;
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
        if(playListRepository.findByNameAndServer(playlistName, server).isPresent()){
            throw new RuntimeException("Ya se encuentra otra playlist con el nombre que proporcionaste.");
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
    public void addTrackToPlayList(String playlistName, String guildId, String title, String trackIdentifier) {
        ServerEntity server = serverRepository.findById(guildId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el servidor!"));

        PlayListEntity playlist = playListRepository.findByNameAndServer(playlistName, server)
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
    public void deletePlayList(String playlistName, String guildId) {
        ServerEntity server = serverRepository.findById(guildId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el servidor!"));

        PlayListEntity playlist = playListRepository.findByNameAndServer(playlistName, server)
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
    public PlayListEntity viewPlayList(String playlistName, String guildId) {
        ServerEntity server = serverRepository.findById(guildId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el servidor!"));

        PlayListEntity playlist = playListRepository.findByNameAndServer(playlistName, server)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la playlist '" + playlistName + "'."));

        playlist.getItems().size();

        return playlist;
    }

    @Override
    @Transactional
    public PlayListEntity loadPlayList(String playlistName, String guildId){

        ServerEntity server = serverRepository.findById(guildId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el servidor!"));

        PlayListEntity playlist = playListRepository.findByNameAndServer(playlistName, server)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la playlist '" + playlistName + "'."));

        List<PlayListItemEntity> items = playlist.getItems();

        if(items.isEmpty()){
            throw new RuntimeException("La playlist '" + playlistName + "' está vacía.");
        }

        return playlist;

    }
}
