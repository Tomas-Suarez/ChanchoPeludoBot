package com.chanchopeludo.ChanchoPeludoBot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lombok.Data;
import net.dv8tion.jda.api.managers.AudioManager;

@Data
public class GuildMusicManager {

    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;

    private long lastTextChannelId;


    public GuildMusicManager(AudioPlayerManager manager, AudioManager audioManager) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player, this, audioManager);
        player.addListener(scheduler);
        sendHandler = new AudioPlayerSendHandler(player);
    }
}