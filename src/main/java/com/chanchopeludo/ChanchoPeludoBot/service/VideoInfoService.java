package com.chanchopeludo.ChanchoPeludoBot.service;

import com.chanchopeludo.ChanchoPeludoBot.dto.internal.VideoInfo;

import java.util.concurrent.CompletableFuture;

public interface VideoInfoService {

    /**
     * Obtiene la información sobre un video
     * de forma asincrónica.
     *
     * @Param youtubeUrl la Url de youtube
     * @Return Un CompletableFuture que nos devuelve un VideoInfo(Url y Titulo)
     */
    CompletableFuture<VideoInfo> getVideoInfo(String youtubeUrl);
}
