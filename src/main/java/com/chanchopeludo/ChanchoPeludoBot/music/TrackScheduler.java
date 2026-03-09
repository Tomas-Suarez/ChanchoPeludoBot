package com.chanchopeludo.ChanchoPeludoBot.music;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Getter
public class TrackScheduler {
    private final BlockingQueue<Track> queue;
    private final GuildMusicManager musicManager;
    private final Link link;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> disconnectTask;
    private static final Logger log = LoggerFactory.getLogger(TrackScheduler.class);

    public TrackScheduler(GuildMusicManager musicManager, Link link) {
        this.queue = new LinkedBlockingQueue<>();
        this.musicManager = musicManager;
        this.link = link;
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
            log.info("Desconectando por inactividad del guild: {}", link.getGuildId());
            long channelId = musicManager.getLastTextChannelId();

            link.destroy().subscribe();

            if (channelId != 0) {
                log.info("Bot desconectado. Último canal de texto: {}", channelId);
            }
        }, 5, TimeUnit.MINUTES);
    }

    public void cancelDisconnectTask() {
        if (disconnectTask != null && !disconnectTask.isDone()) {
            disconnectTask.cancel(false);
            disconnectTask = null;
            log.debug("Timer de desconexión cancelado para el guild: {}", link.getGuildId());
        }
    }
}