package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.music.GuildMusicManager;
import com.chanchopeludo.ChanchoPeludoBot.music.PlayerManager;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.util.helpers.YoutubeHelper;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
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
        System.setProperty("lavaplayer.youtube.http.userAgent", USER_AGENT);
        this.playerManager = PlayerManager.getInstance().getPlayerManager();
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

    private String getAudioUrl(String youtubeUrl) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                YTDLP_EXECUTABLE,
                YTDLP_FORMAT,
                YTDLP_BEST_AUDIO,
                YTDLP_GET_URL,
                youtubeUrl
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String directUrl = reader.readLine();
            process.waitFor();
            return directUrl;
        }
    }

    @Override
    public void loadAndPlay(SlashCommandInteractionEvent event, String trackUrl) {
        Guild guild = event.getGuild();
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        try {
            String directUrl = getAudioUrl(trackUrl);

            playerManager.loadItemOrdered(musicManager, directUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    musicManager.getScheduler().queue(track);
                    event.getHook().sendMessage(MSG_TRACK_PLAYING + track.getInfo().title).queue();
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    AudioTrack firstTrack = playlist.getSelectedTrack();
                    if (firstTrack == null) firstTrack = playlist.getTracks().get(0);
                    musicManager.getScheduler().queue(firstTrack);
                    event.getHook().sendMessage(
                            MSG_PLAYLIST_LOADED + playlist.getName() +
                                    " (" + playlist.getTracks().size() + " canciones)"
                    ).queue();
                }

                @Override
                public void noMatches() {
                    event.getHook().sendMessage(MSG_NO_MATCHES).queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    event.getHook().sendMessage(MSG_LOAD_FAILED + exception.getMessage()).queue();
                    exception.printStackTrace();
                }
            });

        } catch (IOException | InterruptedException e) {
            event.getHook().sendMessage(MSG_YTDLP_ERROR + e.getMessage()).queue();
        }
    }

    @Override
    public void loadAndPlayFromMessage(MessageReceivedEvent event, String trackUrl) {
        Guild guild = event.getGuild();
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        try {
            String directUrl = YoutubeHelper.getAudioUrl(trackUrl);
            String videoTitle = YoutubeHelper.getVideoTitle(trackUrl);

            playerManager.loadItemOrdered(musicManager, directUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    event.getChannel().sendMessage(MSG_TRACK_ADDED + videoTitle + "**").queue();
                    play(guild, musicManager, track, event.getMember().getVoiceState().getChannel());
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    AudioTrack firstTrack = playlist.getSelectedTrack();
                    if (firstTrack == null) firstTrack = playlist.getTracks().get(0);
                    event.getChannel().sendMessage(
                            MSG_PLAYLIST_ADDED + playlist.getName() +
                                    "** (" + playlist.getTracks().size() + " canciones)"
                    ).queue();
                    play(guild, musicManager, firstTrack, event.getMember().getVoiceState().getChannel());
                }

                @Override
                public void noMatches() {
                    event.getChannel().sendMessage(MSG_NO_MATCHES_URL).queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    event.getChannel().sendMessage(MSG_LOAD_FAILED + exception.getMessage()).queue();
                    exception.printStackTrace();
                }
            });

        } catch (IOException | InterruptedException e) {
            event.getChannel().sendMessage(MSG_YOUTUBE_ERROR + e.getMessage()).queue();
        }
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, AudioChannel voiceChannel) {
        if (voiceChannel == null) return;

        if (!guild.getAudioManager().isConnected()) {
            guild.getAudioManager().openAudioConnection(voiceChannel);
        }
        musicManager.getScheduler().queue(track);
    }
}
