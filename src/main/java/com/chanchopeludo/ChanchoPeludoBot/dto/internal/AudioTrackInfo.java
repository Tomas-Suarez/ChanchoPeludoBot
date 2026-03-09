package com.chanchopeludo.ChanchoPeludoBot.dto.internal;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;

public record AudioTrackInfo(String title, String url, long durationMs, String author) {

    public static AudioTrackInfo fromTrack(Track track) {
        if (track == null){
            return null;
        }

        TrackInfo info = track.getInfo();

        String title = info.getTitle();
        String url = info.getUri();
        long durationMs = info.getLength();
        String author = info.getAuthor();

        return new AudioTrackInfo(title, url, durationMs, author);
    }
}