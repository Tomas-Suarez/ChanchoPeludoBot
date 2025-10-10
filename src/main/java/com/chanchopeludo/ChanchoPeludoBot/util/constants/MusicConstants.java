package com.chanchopeludo.ChanchoPeludoBot.util.constants;

public final class MusicConstants {

    private MusicConstants(){}

    // Mensajes del bot de musica
    public static final String MSG_TRACK_PLAYING = "🎶 Reproduciendo: ";
    public static final String MSG_PLAYLIST_LOADED = "🎶 Lista cargada: ";
    public static final String MSG_TRACK_ADDED = "🎵 Añadiendo a la cola: **";
    public static final String MSG_PLAYLIST_ADDED = "🎶 Añadiendo playlist: **";
    public static final String MSG_NO_MATCHES = "❌ No se encontró la canción.";
    public static final String MSG_NO_MATCHES_URL = "❌ No se encontró nada en la URL.";
    public static final String MSG_LOAD_FAILED = "⚠️ Error al cargar la canción: ";
    public static final String MSG_YTDLP_ERROR = "❌ Error al obtener el audio con yt-dlp: ";
    public static final String MSG_YOUTUBE_ERROR = "❌ Error al procesar YouTube: ";

    // Configuración de Lavaplayer / YouTube
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";

    // Comando yt-dlp
    public static final String YTDLP_EXECUTABLE = "yt-dlp.exe";
    public static final String YTDLP_FORMAT = "-f";
    public static final String YTDLP_BEST_AUDIO = "bestaudio";
    public static final String YTDLP_GET_URL = "-g";
}
