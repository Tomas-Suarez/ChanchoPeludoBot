package com.chanchopeludo.ChanchoPeludoBot.commands.music.handlers;

import java.util.concurrent.CompletableFuture;

public interface InputHandler {

    boolean canHandle(String input);

    CompletableFuture<String> handle(long guildId, long voiceChannelId, long textChannelId, String input);
}