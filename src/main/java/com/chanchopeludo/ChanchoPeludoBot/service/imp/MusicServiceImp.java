package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.dto.internal.AudioTrackInfo;
import com.chanchopeludo.ChanchoPeludoBot.dto.internal.QueueState;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ExternalServiceException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.InvalidInputException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ResourceNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.*;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.ResourceNames.GUILD_DISCORD;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.ResourceNames.VOICE_CHANNEL;

@Slf4j
@Service
public class MusicServiceImp implements MusicService {

    private AudioPlayerManager playerManager;
    private final VideoInfoService videoInfoService;
    private final JDA jda;
    private final Map<Long, GuildMusicManager> musicManagers;

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
            musicManager = new GuildMusicManager(playerManager, guild.getAudioManager());
            musicManagers.put(guild.getIdLong(), musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

    @Override
    public CompletableFuture<String> loadAndPlay(long guildId, long voiceChannelId, long textChannelId, String trackUrl) {
        log.info("PLAY. Guild: {}, URL: {}", guildId, trackUrl);

        CompletableFuture<String> futureResult = new CompletableFuture<>();

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));
        }
        AudioChannel voiceChannel = guild.getChannelById(AudioChannel.class, voiceChannelId);
        if (voiceChannel == null) {
            throw new ResourceNotFoundException(VOICE_CHANNEL, String.valueOf(voiceChannelId));
        }

        final GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        musicManager.setLastTextChannelId(textChannelId);

        videoInfoService.getVideoInfo(trackUrl)
                .thenAccept(info -> {
                    playerManager.loadItemOrdered(musicManager, info.url(), new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            log.info("Track cargado: {}", info.title());
                            track.setUserData(info);
                            play(guild, musicManager, track, voiceChannel);
                            futureResult.complete(MSG_TRACK_ADDED + info.title() + "**");
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            log.info("Playlist cargada: {} ({} canciones)", playlist.getName(), playlist.getTracks().size());
                            play(guild, musicManager, playlist.getTracks().get(0), voiceChannel);
                            futureResult.complete(MSG_PLAYLIST_ADDED + playlist.getName() + "**");
                        }

                        @Override
                        public void noMatches() {
                            log.warn("No se encontraron coincidencias para: {}", trackUrl);
                            futureResult.completeExceptionally(
                                    new ResourceNotFoundException(MSG_NO_MATCHES_URL)
                            );
                        }

                        @Override
                        public void loadFailed(FriendlyException exception) {
                            log.error("LavaPlayer falló al cargar: {}", exception.getMessage(), exception);
                            futureResult.completeExceptionally(
                                    new ExternalServiceException(MSG_LOAD_FAILED, exception.getMessage())
                            );
                        }
                    });
                })
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause();
                    log.error("Error al buscar video", cause != null ? cause : ex);
                    futureResult.completeExceptionally(ex);
                    return null;
                });

        return futureResult;
    }

    @Override
    public String skipTrack(long guildId) {
        log.info("SKIP en Guild {}", guildId);

        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            throw new InvalidInputException(MSG_SKIP_FAIL);
        }

        String skippedTrackTitle = musicManager.getPlayer().getPlayingTrack().getInfo().title;
        musicManager.getScheduler().nextTrack();

        log.info("Canción salteada: {}", skippedTrackTitle);
        return skippedTrackTitle;
    }

    @Override
    public void stop(long guildId) {
        log.info("STOP en Guild {}", guildId);

        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        musicManager.getScheduler().getQueue().clear();
        musicManager.getPlayer().stopTrack();
        guild.getAudioManager().closeAudioConnection();
    }

    @Override
    public void pause(long guildId) {
        log.info("PAUSE en Guild {}", guildId);

        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        if (musicManager.getPlayer().isPaused()) {
            throw new InvalidInputException(MSG_ALREADY_PAUSED);
        }

        musicManager.getPlayer().setPaused(true);
    }

    @Override
    public void resume(long guildId) {
        log.info("RESUME en Guild {}", guildId);

        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        if (!musicManager.getPlayer().isPaused()) {
            throw new InvalidInputException(MSG_NOT_PAUSED);

        }

        musicManager.getPlayer().setPaused(false);
    }

    @Override
    public void volume(long guildId, int valueVolume) {
        log.info("Cambiando VOLUMEN a {} en Guild {}", valueVolume, guildId);

        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        if (valueVolume < 0 || valueVolume > 120) {
            throw new InvalidInputException(MSG_INVALID_VALUE_VOLUME);
        }

        musicManager.getPlayer().setVolume(valueVolume);
    }

    @Override
    public void shuffle(long guildId) {
        log.info("SHUFFLE en Guild {}", guildId);

        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));
        }

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        TrackScheduler scheduler = musicManager.getScheduler();

        if (scheduler.getQueue().isEmpty()) {
            throw new InvalidInputException(MSG_SHUFFLE_FAILED);
        }

        scheduler.shuffle();
    }

    @Override
    public QueueState getQueueState(long guildId) {
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));
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
    public CompletableFuture<Void> queueTrack(long guildId, long textChannelId, String trackUrl) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));
        }

        final GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        musicManager.setLastTextChannelId(textChannelId);

        videoInfoService.getVideoInfo(trackUrl)
                .thenAccept(info -> {
                    playerManager.loadItemOrdered(musicManager, info.url(), new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            track.setUserData(info);
                            musicManager.getScheduler().queue(track);
                            future.complete(null);
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist audioPlaylist) {
                            for (AudioTrack track : audioPlaylist.getTracks()) {
                                musicManager.getScheduler().queue(track);
                            }
                            future.complete(null);
                        }

                        @Override
                        public void noMatches() {
                            log.warn("queueTrack no encontró coincidencias para: {}", trackUrl);
                            future.completeExceptionally(new ResourceNotFoundException("No se encontraron coincidencias: " + trackUrl));

                        }

                        @Override
                        public void loadFailed(FriendlyException e) {
                            log.error("Fallo al cargar la canción en queueTrack: {}", info.url(), e);
                            future.completeExceptionally(new ExternalServiceException("AudioPlayer", "Fallo al cargar en segundo plano"));                        }
                    });
                })
                .exceptionally(ex -> {
                    log.error("Error en queueTrack con yt-dlp para: '{}'", trackUrl, ex);
                    return null;
                });

        return future;
    }

    @Override
    public CompletableFuture<Void> playTrackSilently(long guildId, long voiceChannelId, long textChannelId, String trackUrl) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));
        }

        AudioChannel voiceChannel = guild.getChannelById(AudioChannel.class, voiceChannelId);
        if (voiceChannel == null) {
            throw new ResourceNotFoundException(VOICE_CHANNEL, String.valueOf(voiceChannelId));
        }

        final GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        musicManager.setLastTextChannelId(textChannelId);

        videoInfoService.getVideoInfo(trackUrl)
                .thenAccept(info -> {
                    playerManager.loadItemOrdered(musicManager, info.url(), new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            track.setUserData(info);
                            play(guild, musicManager, track, voiceChannel);
                            future.complete(null);
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            if (!playlist.getTracks().isEmpty()) {
                                play(guild, musicManager, playlist.getTracks().get(0), voiceChannel);
                                future.complete(null);
                            } else {
                                future.completeExceptionally(new InvalidInputException(MSG_QUEUE_EMPTY));
                            }
                        }

                        @Override
                        public void noMatches() {
                            log.warn("playTrackSilently no encontró coincidencias para: {}", trackUrl);
                            future.completeExceptionally(new ResourceNotFoundException("No se encontraron coincidencias: " + trackUrl));
                        }

                        @Override
                        public void loadFailed(FriendlyException exception) {
                            log.error("playTrackSilently falló al cargar: {}", trackUrl, exception);
                            future.completeExceptionally(new ExternalServiceException("AudioPlayer", exception.getMessage()));
                        }
                    });
                })
                .exceptionally(ex -> {
                    log.error("Error en playTrackSilently con yt-dlp para: '{}'", trackUrl, ex);
                    return null;
                });

        return future;
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, AudioChannel voiceChannel) {
        if (voiceChannel == null) {
            log.warn("Intento de Play sin canal de voz en Guild {}", guild.getId());
            return;
        }

        if (!guild.getAudioManager().isConnected()) {
            log.debug("Conectando al canal de voz: {}", voiceChannel.getName());
            guild.getAudioManager().openAudioConnection(voiceChannel);
            guild.getAudioManager().setSelfDeafened(true);
        }
        musicManager.getScheduler().queue(track);
    }
}