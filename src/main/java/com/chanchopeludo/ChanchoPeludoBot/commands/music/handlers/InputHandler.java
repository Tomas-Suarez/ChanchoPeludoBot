package com.chanchopeludo.ChanchoPeludoBot.commands.music.handlers;

import com.chanchopeludo.ChanchoPeludoBot.dto.PlayResult;

import java.util.function.Consumer;

public interface InputHandler {

    boolean canHandle(String input);

    void handle(long guildId, long voiceChannelId, long textChannelId, String input, Consumer<PlayResult> reply);
}