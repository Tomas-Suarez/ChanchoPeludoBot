package com.chanchopeludo.ChanchoPeludoBot.dto.internal;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public record AudioTrackInfo(String title, String url, long durationMs, String author) {

    public static AudioTrackInfo fromAudioTrack(AudioTrack track) {
        if (track == null) return null;

        com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo lavaplayerInfo = track.getInfo();

        String title;
        String url = lavaplayerInfo.uri;
        long durationMs = lavaplayerInfo.length;
        String author = lavaplayerInfo.author;

        if (track.getUserData() instanceof VideoInfo videoInfo) {
            title = videoInfo.title();
        } else {
            title = lavaplayerInfo.title;
        }

        return new AudioTrackInfo(title, url, durationMs, author);
    }
}