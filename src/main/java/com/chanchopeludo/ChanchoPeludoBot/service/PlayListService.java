package com.chanchopeludo.ChanchoPeludoBot.service;

import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListItemEntity;

import java.util.List;

public interface PlayListService {

    void createPlayList(String playlistName, String guildId, String creatorId);

    void addTrackToPlayList(String playlistName, String guildId, String title, String trackIdentifier);

    void deletePlayList(String playlistName, String guildId);

    void removeTrack(String playlistName, int trackOrder, String guildId);

    List<PlayListItemEntity> listPlayLists();

    PlayListEntity loadPlayList(String playlistName, String guildId);
}
