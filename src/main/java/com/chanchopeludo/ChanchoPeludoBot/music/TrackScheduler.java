package com.chanchopeludo.ChanchoPeludoBot.music;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Getter
public class TrackScheduler {
    private final BlockingQueue<Track> queue;
    private final GuildMusicManager musicManager;
    private final Link link;
    private final JDA jda;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> disconnectTask;

    public TrackScheduler(GuildMusicManager musicManager, Link link, JDA jda) {
        this.queue = new LinkedBlockingQueue<>();
        this.musicManager = musicManager;
        this.link = link;
        this.jda = jda;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void queue(Track track) {
        cancelDisconnectTask();

        if (link.getCachedPlayer() == null || link.getCachedPlayer().getTrack() == null) {
            link.createOrUpdatePlayer().setTrack(track).subscribe();
        } else {
            queue.offer(track);
        }
    }

    public void nextTrack() {
        Track track = queue.poll();
        if (track != null) {
            link.createOrUpdatePlayer().setTrack(track).subscribe();
        } else {
            link.createOrUpdatePlayer().setTrack(null).subscribe();
            startDisconnectTimer();
        }
    }

    public void shuffle() {
        List<Track> trackList = new ArrayList<>();
        this.queue.drainTo(trackList);
        Collections.shuffle(trackList);
        this.queue.addAll(trackList);
    }

    private void startDisconnectTimer() {
        cancelDisconnectTask();
        log.info("Cola de reproducción vacía. Timer de desconexión iniciado para el guild: {}", link.getGuildId());

        disconnectTask = executor.schedule(() -> {
            long guildId = link.getGuildId();
            log.info("Desconectando por inactividad musical del guild: {}", guildId);

            link.destroy().subscribe();

            Guild guild = jda.getGuildById(guildId);
            if (guild != null) {
                guild.getAudioManager().closeAudioConnection();
            }

            long channelId = musicManager.getLastTextChannelId();
            if (channelId != 0) {
                log.info("Bot desconectado. Último canal de texto: {}", channelId);
            }
        }, 5, TimeUnit.MINUTES);
    }

    public void cancelDisconnectTask() {
        if (disconnectTask != null && !disconnectTask.isDone()) {
            disconnectTask.cancel(false);
            disconnectTask = null;
            log.info("Timer de desconexión cancelado para el guild: {}", link.getGuildId());
        }
    }
}