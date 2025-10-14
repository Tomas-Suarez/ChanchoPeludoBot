package com.chanchopeludo.ChanchoPeludoBot.dto;

public record SpotifyTrack(String name, String artist) {
    /**
     * Nos devuelve un texto del nombre y el artista.
     * Para ser luego buscado por Youtube.
     *
     * @return "ytsearch:Nombre de la Cancion Artista"
     */
    public String toYoutubeSearchQuery() {
        return "ytsearch:" + this.name + " " + this.artist;
    }
}
