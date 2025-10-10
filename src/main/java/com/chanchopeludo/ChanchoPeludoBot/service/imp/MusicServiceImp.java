package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.music.GuildMusicManager;
import com.chanchopeludo.ChanchoPeludoBot.music.VideoInfo;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.*;

@Service
public class MusicServiceImp implements MusicService {

    private AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public MusicServiceImp() {
        this.musicManagers = new HashMap<>();
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

    private VideoInfo getVideoInfo(String youtubeUrl) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp",
                "--no-update",
                "-4",
                "--print", "%(title)s\n%(url)s",
                "-f", "bestaudio[ext=m4a]/bestaudio/best",
                youtubeUrl
        );
        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String title = reader.readLine();
            String directUrl = reader.readLine();
            process.waitFor();
            if (title == null || directUrl == null) {
                return null;
            }
            return new VideoInfo(title, directUrl);
        }
    }

    @Override
    public void loadAndPlay(SlashCommandInteractionEvent event, String trackUrl) {
        event.getHook().sendMessage(MSG_SEARCH_MUSIC).queue();
        CompletableFuture.runAsync(() -> {
            try {
                final Guild guild = event.getGuild();
                final GuildMusicManager musicManager = getGuildAudioPlayer(guild);
                final VideoInfo info = getVideoInfo(trackUrl);
                if (info == null) {
                    event.getHook().sendMessage(MSG_NO_MATCHES_URL).queue();
                    return;
                }
                playerManager.loadItemOrdered(musicManager, info.url(), new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        event.getHook().sendMessage(MSG_TRACK_ADDED + info.title() + "**").queue();
                        play(guild, musicManager, track, event.getMember().getVoiceState().getChannel());
                    }
                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        AudioTrack firstTrack = playlist.getTracks().get(0);
                        event.getHook().sendMessage(MSG_PLAYLIST_ADDED + playlist.getName() + "** (" + playlist.getTracks().size() + " canciones)").queue();
                        play(guild, musicManager, firstTrack, event.getMember().getVoiceState().getChannel());
                    }
                    @Override
                    public void noMatches() {
                        event.getHook().sendMessage(MSG_NO_MATCHES_URL).queue();
                    }
                    @Override
                    public void loadFailed(FriendlyException exception) {
                        event.getHook().sendMessage(MSG_LOAD_FAILED + exception.getMessage()).queue();
                    }
                });
            } catch (IOException | InterruptedException e) {
                event.getHook().sendMessage(MSG_YOUTUBE_ERROR + e.getMessage()).queue();
            }
        });
    }

    @Override
    public void loadAndPlayFromMessage(MessageReceivedEvent event, String trackUrl) {
        event.getChannel().sendMessage(MSG_SEARCH_MUSIC).queue();
        CompletableFuture.runAsync(() -> {
            try {
                final Guild guild = event.getGuild();
                final GuildMusicManager musicManager = getGuildAudioPlayer(guild);
                final VideoInfo info = getVideoInfo(trackUrl);
                if (info == null) {
                    event.getChannel().sendMessage(MSG_NO_MATCHES_URL).queue();
                    return;
                }
                playerManager.loadItemOrdered(musicManager, info.url(), new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        event.getChannel().sendMessage(MSG_TRACK_ADDED + info.title() + "**").queue();
                        play(guild, musicManager, track, event.getMember().getVoiceState().getChannel());
                    }
                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        AudioTrack firstTrack = playlist.getTracks().get(0);
                        event.getChannel().sendMessage(MSG_PLAYLIST_ADDED + playlist.getName() + "** (" + playlist.getTracks().size() + " canciones)").queue();
                        play(guild, musicManager, firstTrack, event.getMember().getVoiceState().getChannel());
                    }
                    @Override
                    public void noMatches() {
                        event.getChannel().sendMessage(MSG_NO_MATCHES_URL).queue();
                    }
                    @Override
                    public void loadFailed(FriendlyException exception) {
                        event.getChannel().sendMessage(MSG_LOAD_FAILED + exception.getMessage()).queue();
                    }
                });
            } catch (IOException | InterruptedException e) {
                event.getChannel().sendMessage(MSG_YOUTUBE_ERROR + e.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, AudioChannel voiceChannel) {
        if (voiceChannel == null) return;
        if (!guild.getAudioManager().isConnected()) {
            guild.getAudioManager().openAudioConnection(voiceChannel);
        }
        musicManager.getScheduler().queue(track);
    }
}