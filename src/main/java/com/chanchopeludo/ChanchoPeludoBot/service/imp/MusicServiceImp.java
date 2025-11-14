package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.dto.AudioTrackInfo;
import com.chanchopeludo.ChanchoPeludoBot.dto.PlayResult;
import com.chanchopeludo.ChanchoPeludoBot.dto.QueueState;
import com.chanchopeludo.ChanchoPeludoBot.music.GuildMusicManager;
import com.chanchopeludo.ChanchoPeludoBot.music.TrackScheduler;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.VideoInfoService;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.GenericConstants.SERVER_NOT_FOUND;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.*;

@Service
public class MusicServiceImp implements MusicService {

    private AudioPlayerManager playerManager;
    private final VideoInfoService videoInfoService;
    private final JDA jda;
    private final Map<Long, GuildMusicManager> musicManagers;
    private static final Logger logger = LoggerFactory.getLogger(MusicServiceImp.class);


    public MusicServiceImp(VideoInfoService videoInfoService, JDA jda) {
        this.musicManagers = new HashMap<>();
        this.videoInfoService = videoInfoService;
        this.jda = jda;
    }

    @PostConstruct
    private void init() {
        this.playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        GuildMusicManager musicManager = musicManagers.get(guild.getIdLong());
        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guild.getIdLong(), musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

    @Override
    public CompletableFuture<PlayResult> loadAndPlay(long guildId, long voiceChannelId, String trackUrl) {
        CompletableFuture<PlayResult> futureResult = new CompletableFuture<>();

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            futureResult.complete(new PlayResult(false, SERVER_NOT_FOUND));
            return futureResult;
        }
        AudioChannel voiceChannel = guild.getChannelById(AudioChannel.class, voiceChannelId);
        if (voiceChannel == null) {
            futureResult.complete(new PlayResult(false, "Error: No se encontró el canal de voz."));
            return futureResult;
        }

        final GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        videoInfoService.getVideoInfo(trackUrl)
                .thenAccept(info -> {
                    playerManager.loadItemOrdered(musicManager, info.url(), new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            track.setUserData(info);
                            play(guild, musicManager, track, voiceChannel);
                            futureResult.complete(new PlayResult(true, MSG_TRACK_ADDED + info.title() + "**"));
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            play(guild, musicManager, playlist.getTracks().get(0), voiceChannel);
                            futureResult.complete(new PlayResult(true, MSG_PLAYLIST_ADDED + playlist.getName() + "**"));
                        }

                        @Override
                        public void noMatches() {
                            futureResult.complete(new PlayResult(false, MSG_NO_MATCHES_URL));
                        }

                        @Override
                        public void loadFailed(FriendlyException exception) {
                            futureResult.complete(new PlayResult(false, MSG_LOAD_FAILED + exception.getMessage()));
                        }
                    });
                })
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause();
                    logger.error("Error al buscar video", cause != null ? cause : ex);
                    futureResult.complete(new PlayResult(false, MSG_YOUTUBE_ERROR + (cause != null ? cause.getMessage() : ex.getMessage())));
                    return null;
                });

        return futureResult;
    }

    @Override
    public PlayResult skipTrack(long guildId) {
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            return new PlayResult(false, SERVER_NOT_FOUND);
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            return new PlayResult(false, MSG_SKIP_FAIL);
        }

        musicManager.getScheduler().nextTrack();
        return new PlayResult(true, MSG_SKIP_MUSIC);
    }

    @Override
    public PlayResult stop(long guildId) {
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            return new PlayResult(false, SERVER_NOT_FOUND);
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        musicManager.getScheduler().getQueue().clear();
        musicManager.getPlayer().stopTrack();
        guild.getAudioManager().closeAudioConnection();
        return new PlayResult(true, MSG_STOP_MUSIC);
    }

    @Override
    public PlayResult pause(long guildId) {
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            return new PlayResult(false, SERVER_NOT_FOUND);
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        if (musicManager.getPlayer().isPaused()) {
            return new PlayResult(false, MSG_ALREADY_PAUSED);
        }

        musicManager.getPlayer().setPaused(true);
        return new PlayResult(true, MSG_PAUSE_MUSIC);
    }

    @Override
    public PlayResult resume(long guildId) {
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            return new PlayResult(false, SERVER_NOT_FOUND);
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        if (!musicManager.getPlayer().isPaused()) {
            return new PlayResult(false, MSG_NOT_PAUSED);
        }

        musicManager.getPlayer().setPaused(false);
        return new PlayResult(true, MSG_RESUME_MUSIC);
    }

    @Override
    public PlayResult volume(long guildId, int valueVolume) {
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            return new PlayResult(false, SERVER_NOT_FOUND);
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        if (valueVolume < 0 || valueVolume > 100) {
            return new PlayResult(false, MSG_INVALID_VALUE_VOLUME);
        }

        musicManager.getPlayer().setVolume(valueVolume);
        return new PlayResult(true, String.format(MSG_VOLUME_MUSIC, valueVolume));
    }

    @Override
    public PlayResult shuffle(long guildId) {
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            return new PlayResult(false, SERVER_NOT_FOUND);
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        TrackScheduler scheduler = musicManager.getScheduler();

        if (scheduler.getQueue().isEmpty()) {
            return new PlayResult(false, MSG_SHUFFLE_FAILED);
        }

        scheduler.shuffle();

        return new PlayResult(true, MSG_SHUFFLE_PLAYLIST);
    }

    @Override
    public QueueState getQueueState(long guildId) {
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            return new QueueState(null, 0, List.of());
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        AudioTrack nowPlayingTrack = musicManager.getPlayer().getPlayingTrack();

        AudioTrackInfo nowPlayingDto = AudioTrackInfo.fromAudioTrack(nowPlayingTrack);

        long position = (nowPlayingTrack != null) ? nowPlayingTrack.getPosition() : 0;

        List<AudioTrackInfo> queueDtoList = musicManager.getScheduler().getQueue().stream()
                .map(AudioTrackInfo::fromAudioTrack)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new QueueState(nowPlayingDto, position, queueDtoList);
    }

    @Override
    public CompletableFuture<PlayResult> queueTrack(long guildId, String trackUrl) {
        CompletableFuture<PlayResult> future = new CompletableFuture<>();

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            future.complete(new PlayResult(false, "Servidor no encontrado."));
            return future;
        }

        final GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        videoInfoService.getVideoInfo(trackUrl)
                .thenAccept(info -> {
                    playerManager.loadItemOrdered(musicManager, info.url(), new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            track.setUserData(info);
                            musicManager.getScheduler().queue(track);
                            future.complete(new PlayResult(true, "Canción encolada: " + info.title()));
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist audioPlaylist) {
                            for (AudioTrack track : audioPlaylist.getTracks()) {
                                musicManager.getScheduler().queue(track);
                            }
                            future.complete(new PlayResult(true, "Playlist encolada: " + audioPlaylist.getName()));
                        }

                        @Override
                        public void noMatches() {
                            logger.warn("queueTrack no encontró coincidencias para: {}", trackUrl);
                            future.complete(new PlayResult(false, "No se encontraron coincidencias."));
                        }

                        @Override
                        public void loadFailed(FriendlyException e) {
                            logger.error("Fallo al cargar la canción en queueTrack: {}", info.url(), e);
                            future.complete(new PlayResult(false, "Fallo al cargar la canción."));
                        }
                    });
                })
                .exceptionally(ex -> {
                    logger.error("Error en queueTrack con yt-dlp para: '{}'", trackUrl, ex);
                    future.complete(new PlayResult(false, "Error al procesar la URL."));
                    return null;
                });

        return future;
    }

    @Override
    public CompletableFuture<PlayResult> playTrackSilently(long guildId, long voiceChannelId, String trackUrl) {
        CompletableFuture<PlayResult> future = new CompletableFuture<>();

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            future.complete(new PlayResult(false, "Servidor no encontrado."));
            return future;
        }
        AudioChannel voiceChannel = guild.getChannelById(AudioChannel.class, voiceChannelId);
        if (voiceChannel == null) {
            future.complete(new PlayResult(false, "Canal de voz no encontrado."));
            return future;
        }

        final GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        videoInfoService.getVideoInfo(trackUrl)
                .thenAccept(info -> {
                    playerManager.loadItemOrdered(musicManager, info.url(), new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            track.setUserData(info);
                            play(guild, musicManager, track, voiceChannel);
                            future.complete(new PlayResult(true, "Reproduciendo silenciosamente: " + info.title()));
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            if (!playlist.getTracks().isEmpty()) {
                                play(guild, musicManager, playlist.getTracks().get(0), voiceChannel);
                                future.complete(new PlayResult(true, "Reproduciendo playlist silenciosamente."));
                            } else {
                                future.complete(new PlayResult(false, "Playlist vacía."));
                            }
                        }

                        @Override
                        public void noMatches() {
                            logger.warn("playTrackSilently no encontró coincidencias para: {}", trackUrl);
                            future.complete(new PlayResult(false, "No se encontraron coincidencias."));
                        }

                        @Override
                        public void loadFailed(FriendlyException exception) {
                            logger.error("playTrackSilently falló al cargar: {}", trackUrl, exception);
                            future.complete(new PlayResult(false, "Fallo al cargar la canción."));
                        }
                    });
                })
                .exceptionally(ex -> {
                    logger.error("Error en playTrackSilently con yt-dlp para: '{}'", trackUrl, ex);
                    future.complete(new PlayResult(false, "Error al procesar la URL."));
                    return null;
                });

        return future;
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, AudioChannel voiceChannel) {
        if (voiceChannel == null) {
            logger.warn("El usuario no estaba en un canal de voz al intentar reproducir.");
            return;
        }

        guild.getAudioManager().openAudioConnection(voiceChannel);
        guild.getAudioManager().setSelfDeafened(true);
        musicManager.getScheduler().queue(track);
    }
}