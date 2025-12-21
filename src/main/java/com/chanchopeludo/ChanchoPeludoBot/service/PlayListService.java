package com.chanchopeludo.ChanchoPeludoBot.service;

import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;

import java.util.List;

public interface PlayListService {

    void createPlayList(String playlistName, String guildId, String creatorId);

    void addTrackToPlayList(String playlistName, String guildId, String userId, String title, String trackIdentifier);

    void deletePlayList(String playlistName, String guildId, String userId);

    String removeTrack(String playlistName, int trackPosition, String guildId, String userId);

    List<PlayListEntity> viewPlayList(String playlistName, String guildId, String userId);

    PlayListEntity loadPlayListById(Long playlistId, String requesterId);

    void renamePlayList(String oldPlayListName, String newPlayListName, String guildId, String userId);

    void updateVisibility(String playListName, boolean isPublic, String guidId, String userId);

    List<PlayListEntity> getPublicPlayLists(String guildId);

    List<PlayListEntity> getUserPlayLists(String userId, String guildId);
}
