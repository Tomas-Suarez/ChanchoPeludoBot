package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.dto.internal.AudioTrackInfo;
import com.chanchopeludo.ChanchoPeludoBot.dto.internal.QueueState;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ExternalServiceException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.InvalidInputException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ResourceNotFoundException;
import com.chanchopeludo.ChanchoPeludoBot.music.GuildMusicManager;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.*;
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

    private final LavalinkClient lavalinkClient;
    private final JDA jda;
    private final Map<Long, GuildMusicManager> musicManagers;

    public MusicServiceImp(LavalinkClient lavalinkClient, JDA jda) {
        this.lavalinkClient = lavalinkClient;
        this.jda = jda;
        this.musicManagers = new HashMap<>();
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            Link link = lavalinkClient.getOrCreateLink(guildId);
            musicManager = new GuildMusicManager(link);
            musicManagers.put(guildId, musicManager);
        }

        return musicManager;
    }

    @Override
    public CompletableFuture<String> loadAndPlay(long guildId, long voiceChannelId, long textChannelId, String trackUrl) {
        log.info("PLAY. Guild: {}, URL: {}", guildId, trackUrl);
        CompletableFuture<String> futureResult = new CompletableFuture<>();

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));

        AudioChannel voiceChannel = guild.getChannelById(AudioChannel.class, voiceChannelId);
        if (voiceChannel == null) throw new ResourceNotFoundException(VOICE_CHANNEL, String.valueOf(voiceChannelId));

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.setLastTextChannelId(textChannelId);

        musicManager.getLink().loadItem(trackUrl).subscribe(
                result -> {
                    if (result instanceof TrackLoaded loaded) {
                        Track track = loaded.getTrack();
                        log.info("Track cargado: {}", track.getInfo().getTitle());
                        play(guild, musicManager, track, voiceChannel);
                        futureResult.complete(MSG_TRACK_ADDED + track.getInfo().getTitle() + "**");

                    } else if (result instanceof PlaylistLoaded playlist) {
                        log.info("Playlist cargada: {} ({} canciones)", playlist.getInfo().getName(), playlist.getTracks().size());
                        play(guild, musicManager, playlist.getTracks().get(0), voiceChannel);
                        futureResult.complete(MSG_PLAYLIST_ADDED + playlist.getInfo().getName() + "**");

                    } else if (result instanceof SearchResult search) {
                        List<Track> tracks = search.getTracks();
                        if (tracks.isEmpty()) {
                            futureResult.completeExceptionally(new ResourceNotFoundException(MSG_NO_MATCHES_URL));
                            return;
                        }
                        play(guild, musicManager, tracks.get(0), voiceChannel);
                        futureResult.complete(MSG_TRACK_ADDED + tracks.get(0).getInfo().getTitle() + "**");

                    } else if (result instanceof NoMatches) {
                        log.warn("No se encontraron coincidencias para: {}", trackUrl);
                        futureResult.completeExceptionally(new ResourceNotFoundException(MSG_NO_MATCHES_URL));

                    } else if (result instanceof LoadFailed failed) {
                        log.error("Lavalink falló al cargar: {}", failed.getException().getMessage());
                        futureResult.completeExceptionally(new ExternalServiceException(MSG_LOAD_FAILED, failed.getException().getMessage()));
                    }
                },
                error -> {
                    log.error("Error al comunicarse con el nodo de Lavalink", error);
                    futureResult.completeExceptionally(new ExternalServiceException("LavalinkNode", error.getMessage()));
                }
        );

        return futureResult;
    }

    @Override
    public String skipTrack(long guildId) {
        log.info("SKIP en Guild {}", guildId);
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        LavalinkPlayer player = musicManager.getLink().getCachedPlayer();

        if (player == null || player.getTrack() == null) {
            throw new InvalidInputException(MSG_SKIP_FAIL);
        }

        String skippedTrackTitle = player.getTrack().getInfo().getTitle();
        musicManager.getScheduler().nextTrack();

        log.info("Canción salteada: {}", skippedTrackTitle);
        return skippedTrackTitle;
    }

    @Override
    public void stop(long guildId) {
        log.info("STOP en Guild {}", guildId);
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.getScheduler().getQueue().clear();

        musicManager.getLink().createOrUpdatePlayer().setTrack(null).subscribe();

        jda.getDirectAudioController().disconnect(guild);
    }

    @Override
    public void pause(long guildId) {
        log.info("PAUSE en Guild {}", guildId);
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        LavalinkPlayer player = musicManager.getLink().getCachedPlayer();

        if (player != null && player.getPaused()) throw new InvalidInputException(MSG_ALREADY_PAUSED);
        musicManager.getLink().createOrUpdatePlayer().setPaused(true).subscribe();
    }

    @Override
    public void resume(long guildId) {
        log.info("RESUME en Guild {}", guildId);
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        LavalinkPlayer player = musicManager.getLink().getCachedPlayer();

        if (player == null || !player.getPaused()) throw new InvalidInputException(MSG_NOT_PAUSED);
        musicManager.getLink().createOrUpdatePlayer().setPaused(false).subscribe();
    }

    @Override
    public void volume(long guildId, int valueVolume) {
        log.info("Cambiando VOLUMEN a {} en Guild {}", valueVolume, guildId);
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        if (valueVolume < 0 || valueVolume > 120) throw new InvalidInputException(MSG_INVALID_VALUE_VOLUME);

        musicManager.getLink().createOrUpdatePlayer().setVolume(valueVolume).subscribe();
    }

    @Override
    public void shuffle(long guildId) {
        log.info("SHUFFLE en Guild {}", guildId);
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        if (musicManager.getScheduler().getQueue().isEmpty()) throw new InvalidInputException(MSG_SHUFFLE_FAILED);

        musicManager.getScheduler().shuffle();
    }

    @Override
    public QueueState getQueueState(long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        LavalinkPlayer player = musicManager.getLink().getCachedPlayer();

        Track nowPlayingTrack = (player != null) ? player.getTrack() : null;
        AudioTrackInfo nowPlayingDto = AudioTrackInfo.fromTrack(nowPlayingTrack);
        long position = (player != null) ? player.getPosition() : 0;

        List<AudioTrackInfo> queueDtoList = musicManager.getScheduler().getQueue().stream()
                .map(AudioTrackInfo::fromTrack)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new QueueState(nowPlayingDto, position, queueDtoList);
    }

    @Override
    public CompletableFuture<Void> queueTrack(long guildId, long textChannelId, String trackUrl) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.setLastTextChannelId(textChannelId);

        musicManager.getLink().loadItem(trackUrl).subscribe(
                result -> {
                    if (result instanceof TrackLoaded loaded) {
                        musicManager.getScheduler().queue(loaded.getTrack());
                        future.complete(null);
                    } else if (result instanceof PlaylistLoaded playlist) {
                        playlist.getTracks().forEach(track -> musicManager.getScheduler().queue(track));
                        future.complete(null);
                    } else if (result instanceof SearchResult search && !search.getTracks().isEmpty()) {
                        musicManager.getScheduler().queue(search.getTracks().get(0));
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new ResourceNotFoundException("No se encontraron coincidencias o fallo la carga."));
                    }
                },
                error -> future.completeExceptionally(error)
        );

        return future;
    }

    @Override
    public CompletableFuture<Void> playTrackSilently(long guildId, long voiceChannelId, long textChannelId, String trackUrl) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) throw new ResourceNotFoundException(GUILD_DISCORD, String.valueOf(guildId));

        AudioChannel voiceChannel = guild.getChannelById(AudioChannel.class, voiceChannelId);
        if (voiceChannel == null) throw new ResourceNotFoundException(VOICE_CHANNEL, String.valueOf(voiceChannelId));

        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.setLastTextChannelId(textChannelId);

        musicManager.getLink().loadItem(trackUrl).subscribe(
                result -> {
                    if (result instanceof TrackLoaded loaded) {
                        play(guild, musicManager, loaded.getTrack(), voiceChannel);
                        future.complete(null);
                    } else if (result instanceof PlaylistLoaded playlist && !playlist.getTracks().isEmpty()) {
                        play(guild, musicManager, playlist.getTracks().get(0), voiceChannel);
                        future.complete(null);
                    } else if (result instanceof SearchResult search && !search.getTracks().isEmpty()) {
                        play(guild, musicManager, search.getTracks().get(0), voiceChannel);
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new InvalidInputException(MSG_QUEUE_EMPTY));
                    }
                },
                error -> future.completeExceptionally(error)
        );

        return future;
    }

    private void play(Guild guild, GuildMusicManager musicManager, Track track, AudioChannel voiceChannel) {
        if (voiceChannel == null) {
            log.warn("Intento de Play sin canal de voz en Guild {}", guild.getId());
            return;
        }

        if (guild.getSelfMember().getVoiceState() == null || !guild.getSelfMember().getVoiceState().inAudioChannel()) {
            log.debug("Conectando al canal de voz: {}", voiceChannel.getName());

            jda.getDirectAudioController().connect(voiceChannel);
        }

        musicManager.getScheduler().queue(track);
    }
}