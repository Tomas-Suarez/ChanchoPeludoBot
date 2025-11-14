package com.chanchopeludo.ChanchoPeludoBot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildDisconnectEmbed;

@Getter
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    private final GuildMusicManager musicManager;
    private final AudioManager audioManager;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> disconnectTask;
    private static final Logger log = LoggerFactory.getLogger(TrackScheduler.class);

    public TrackScheduler(AudioPlayer player, GuildMusicManager musicManager, AudioManager audioManager) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.musicManager = musicManager;
        this.audioManager = audioManager;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void queue(AudioTrack track) {
        cancelDisconnectTask();

        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void nextTrack() {
        AudioTrack track = queue.poll();
        player.startTrack(track, false);

        if (track == null) {
            startDisconnectTimer();
        }
    }

    public void shuffle(){
        List<AudioTrack> trackList = new ArrayList<>();
        this.queue.drainTo(trackList);

        Collections.shuffle(trackList);

        this.queue.addAll(trackList);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    private void startDisconnectTimer() {
        cancelDisconnectTask();
        log.info("Cola de reproducción vacía. El bot se desconectará en 5 minutos del guild: {}", audioManager.getGuild().getId());

        disconnectTask = executor.schedule(() -> {
            log.info("Desconectando por inactividad del guild: {}", audioManager.getGuild().getId());

            long channelId = musicManager.getLastTextChannelId();
            if (channelId != 0) {
                TextChannel channel = audioManager.getJDA().getTextChannelById(channelId);
                if (channel != null) {
                    MessageEmbed embed = buildDisconnectEmbed();
                    channel.sendMessageEmbeds(embed).queue();
                } else {
                    log.warn("No se pudo encontrar el TextChannel con ID {} para enviar el mensaje de desconexión.", channelId);
                }
            }

            audioManager.closeAudioConnection();
        }, 5, TimeUnit.MINUTES);
    }

    private void cancelDisconnectTask() {
        if (disconnectTask != null && !disconnectTask.isDone()) {
            disconnectTask.cancel(false);
            disconnectTask = null;
            log.debug("Timer de desconexión cancelado para el guild: {}", audioManager.getGuild().getId());
        }
    }
}