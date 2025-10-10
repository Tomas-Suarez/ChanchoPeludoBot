package com.chanchopeludo.ChanchoPeludoBot.util.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class YoutubeHelper {

    public static String getAudioUrl(String youtubeUrl) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("yt-dlp.exe", "-f", "bestaudio", "-g", youtubeUrl);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String directUrl = reader.readLine();
        process.waitFor();
        return directUrl;
    }

    public static String getVideoTitle(String youtubeUrl) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("yt-dlp.exe", "--no-warnings", "--skip-download", "--print", "%(title)s", youtubeUrl);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String title = reader.readLine();
        process.waitFor();
        return title != null ? title.trim() : "Desconocido";
    }
}
