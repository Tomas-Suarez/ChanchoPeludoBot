package com.chanchopeludo.ChanchoPeludoBot.service;

import com.chanchopeludo.ChanchoPeludoBot.dto.SpotifyTrack;

import java.util.Optional;

public interface SpotifyService {

    public Optional<SpotifyTrack> getTrackFromUrl(String url);
}
