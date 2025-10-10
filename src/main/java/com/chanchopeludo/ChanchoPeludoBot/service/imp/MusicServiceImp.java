package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.music.GuildMusicManager;
import com.chanchopeludo.ChanchoPeludoBot.music.PlayerManager;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
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

@Service
public class MusicServiceImp implements MusicService {

    private AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public MusicServiceImp() {
        this.musicManagers = new HashMap<>();
    }

    @PostConstruct
    private void init() {
        // Opcional: forzar un user-agent moderno para YouTube
        System.setProperty("lavaplayer.youtube.http.userAgent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        // Usar PlayerManager centralizado
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
        ProcessBuilder pb = new ProcessBuilder("yt-dlp.exe", "-f", "bestaudio", "-g", youtubeUrl);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String directUrl = reader.readLine();
        process.waitFor();
        return directUrl;
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
                    event.getHook().sendMessage("🎶 Reproduciendo: " + track.getInfo().title).queue();
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    AudioTrack firstTrack = playlist.getSelectedTrack();
                    if (firstTrack == null) firstTrack = playlist.getTracks().get(0);
                    musicManager.getScheduler().queue(firstTrack);
                    event.getHook().sendMessage("🎶 Lista cargada: " + playlist.getName() + " (" + playlist.getTracks().size() + " canciones)").queue();
                }

                @Override
                public void noMatches() {
                    event.getHook().sendMessage("❌ No se encontró la canción.").queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    event.getHook().sendMessage("❌ Error al cargar la canción: " + exception.getMessage()).queue();
                    exception.printStackTrace();
                }
            });

        } catch (IOException | InterruptedException e) {
            event.getHook().sendMessage("❌ Error al obtener el audio con yt-dlp: " + e.getMessage()).queue();
        }
    }

    @Override
    public void loadAndPlayFromMessage(MessageReceivedEvent event, String trackUrl) {
        Guild guild = event.getGuild();
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        try {
            String directUrl = getAudioUrl(trackUrl);

            playerManager.loadItemOrdered(musicManager, directUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    event.getChannel().sendMessage("Añadiendo a la cola: **" + track.getInfo().title + "**").queue();
                    play(guild, musicManager, track, event.getMember().getVoiceState().getChannel());
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    AudioTrack firstTrack = playlist.getSelectedTrack();
                    if (firstTrack == null) firstTrack = playlist.getTracks().get(0);
                    event.getChannel().sendMessage("Añadiendo la playlist: **" + playlist.getName() + "** (" + playlist.getTracks().size() + " canciones)").queue();
                    play(guild, musicManager, firstTrack, event.getMember().getVoiceState().getChannel());
                }

                @Override
                public void noMatches() {
                    event.getChannel().sendMessage("No encontré nada en la URL proporcionada.").queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    event.getChannel().sendMessage("Error al cargar la canción: " + exception.getMessage()).queue();
                    exception.printStackTrace();
                }
            });

        } catch (IOException | InterruptedException e) {
            event.getChannel().sendMessage("❌ Error al obtener el audio con yt-dlp: " + e.getMessage()).queue();
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
