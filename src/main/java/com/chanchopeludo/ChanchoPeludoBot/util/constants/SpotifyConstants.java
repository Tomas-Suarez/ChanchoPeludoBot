package com.chanchopeludo.ChanchoPeludoBot.util.constants;

public final class SpotifyConstants {

    private SpotifyConstants(){}

    //Spotify config
    public static String TOKEN_SUCCESS = "Token de acceso de Spotify obtenido con exito.";
    public static String TOKEN_FAILURE = "Error fatal al obtener el token de acceso de Spotify.";

    // Spotify service
    public static final String URL_NO_ID_WARN = "No se pudo encontrar un ID de track de Spotify en la URL: {}";
    public static final String URL_INVALID_TRACK_ID = "URL de Spotify no contiene un ID de track válido.";
    public static final String SPOTIFY_SEARCH_INFO = "Buscando información en Spotify para el track ID: {}";
    public static final String SPOTIFY_TRACK_FOUND_INFO = "Track encontrado: '{}' por '{}'";
    public static final String ERROR_ASYNC_SPOTIFY = "Error al procesar la URL de Spotify de forma asíncrona";
    public static final String UNKNOWN_ARTIST = "Artista Desconocido";

    // Play command
    public static final String SPOTIFY_URL_DETECTED = "URL de Spotify detectada: {}";
    public static final String SPOTIFY_URL_TRANSLATED = "URL de Spotify traducida a búsqueda de YouTube: {}";
}
