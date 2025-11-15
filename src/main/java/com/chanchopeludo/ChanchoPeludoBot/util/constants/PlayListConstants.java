package com.chanchopeludo.ChanchoPeludoBot.util.constants;

public final class PlayListConstants {

    private PlayListConstants(){}

    // Mensajes de uso
    public static final String PLAYLIST_USAGE_CREATE = "Uso correcto: `c!playlist create <Nombre>`";
    public static final String PLAYLIST_USAGE_ADD = "Uso correcto: `c!playlist add <Nombre-Playlist> <URL o Búsqueda>`";
    public static final String PLAYLIST_USAGE_LOAD = "Uso correcto: `c!playlist load <Nombre-Playlist>`";
    public static final String PLAYLIST_USAGE_DELETE = "Uso correcto: `c!playlist delete <Nombre-Playlist>`";


    //Títulos para embeds
    public static final String TITLE_ERROR_PLAYLIST = "Error playlist";
    public static final String TITLE_PLAYLIST_CREATED = "Playlist Creada";
    public static final String TITLE_PLAYLIST_DELETED = "Playlist Eliminada";
    public static final String TITLE_TRACK_ADDED = "Canción añadida";
    public static final String TITLE_ERROR_PLAYLIST_ADD = "Error al añadir canción";

    // Descripciones para embeds
    public static final String DESC_PLAYLIST_CREATED = "La playlist **%s** ha sido creada con éxito.";
    public static final String DESC_PLAYLIST_DELETED = "La playlist **%s** ha sido eliminada con éxito.";
    public static final String DESC_TRACK_ADDED = "Se añadió **%s** a la playlist **%s**.";



}
