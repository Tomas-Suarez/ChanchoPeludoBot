package com.chanchopeludo.ChanchoPeludoBot.util.constants;

public final class MusicConstants {

    private MusicConstants(){}

    // Mensajes de Búsqueda y Estado
    public static final String MSG_SEARCH_MUSIC = "🔍 Buscando...";
    public static final String MSG_SPOTIFY_PROCESSING = "Procesando enlace de Spotify... 🎶";
    public static final String MSG_PLAYLIST_ADDED_COUNT = "✅ Añadiendo **%d** canciones de la playlist a la cola.";
    public static final String MSG_TRACK_ADDED = "✅ Añadido a la cola: **";
    public static final String MSG_PLAYLIST_ADDED = "✅ Añadiendo playlist: **";
    public static final String MSG_VOLUME_MUSIC = "🔊 Volumen establecido a **%d%%**";
    public static final String MSG_SHUFFLE_PLAYLIST = "🔀 ¡Cola de reproduccion mezclada!";
    public static final String MSG_NOTHING_PLAYING = "🔈 No hay ninguna canción sonando.";

    // Mensajes de Control de Reproducción
    public static final String MSG_NOW_PLAYING = "🎶 Reproduciendo ahora: ";
    public static final String MSG_SKIP_MUSIC = "⏭️ Canción saltada.";
    public static final String MSG_STOP_MUSIC = "⏹️ Reproducción detenida y cola de reproducción limpia.";
    public static final String MSG_PAUSE_MUSIC = "⏸️ Reproducción pausada.";
    public static final String MSG_RESUME_MUSIC = "▶️ Reproducción reanudada.";

    // Mensajes de Error (Fallo del Sistema)
    public static final String MSG_LOAD_FAILED = "⚠️ Error al cargar la canción: ";
    public static final String MSG_YOUTUBE_ERROR = "❌ Error al procesar la solicitud de YouTube: ";
    public static final String MSG_NO_MATCHES_URL = "❌ No se encontró nada en la URL proporcionada.";
    public static final String MSG_SPOTIFY_FAILURE = "No pude encontrar la información de esa canción en Spotify.";

    // Mensajes de Error (Error del Usuario)
    public static final String MSG_NOT_IN_VOICE_CHANNEL = "❌ Debes estar en un canal de voz para usar este comando.";
    public static final String MSG_PLAY_USAGE = "❌ Uso correcto: `c!play <URL de YouTube>`";
    public static final String MSG_SKIP_FAIL = "❌ No hay más canciones en la cola para saltar.";
    public static final String MSG_ALREADY_PAUSED = "⚠️ El reproductor ya está en pausa.";
    public static final String MSG_NOT_PAUSED = "⚠️ El reproductor no está en pausa.";
    public static final String MSG_INVALID_VALUE_VOLUME = "❌ Uso correcto: `c!volume <1-120>`";
    public static final String MSG_SHUFFLE_FAILED = "❌ La cola de reproducciones está vacía, no hay nada que mezclar.";

    // Mensajes para el Comando de Cola (Queue)
    public static final String MSG_QUEUE_TITLE = "🎶 Cola de Reproducción";
    public static final String MSG_QUEUE_EMPTY = "La cola de reproducción está vacía.";
    public static final String MSG_QUEUE_NEXT_UP = "A continuación:";
    public static final String MSG_QUEUE_FOOTER = "Página %d / %d (%d canciones en total)";


}