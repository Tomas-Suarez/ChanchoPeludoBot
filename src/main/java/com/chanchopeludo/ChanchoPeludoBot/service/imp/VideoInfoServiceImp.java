package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.dto.internal.VideoInfo;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ExternalServiceException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ResourceNotFoundException;
import com.chanchopeludo.ChanchoPeludoBot.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class VideoInfoServiceImp implements VideoInfoService {

    @Override
    public CompletableFuture<VideoInfo> getVideoInfo(String youtubeUrl) {
        return CompletableFuture.supplyAsync(() -> {
            ProcessBuilder pb = new ProcessBuilder(
                    "./yt-dlp_linux",
                    "--no-update",
                    "-4",
                    "--print", "%(title)s\n%(url)s",
                    "-f", "bestaudio[ext=m4a]/bestaudio/best",
                    youtubeUrl
            );

            try {
                Process process = pb.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String title = reader.readLine();
                    String directUrl = reader.readLine();
                    process.waitFor();

                    if (title == null || directUrl == null) {
                        throw new ResourceNotFoundException("Video", youtubeUrl);
                    }

                    return new VideoInfo(title, directUrl);
                }
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                log.error("Error ejecutando ./yt-dlp_linux para la URL: {}", youtubeUrl, e);

                throw new ExternalServiceException("YouTube", "No se pudo procesar el audio del video.");
            }
        });
    }
}