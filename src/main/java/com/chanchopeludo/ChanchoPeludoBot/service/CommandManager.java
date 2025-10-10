package com.chanchopeludo.ChanchoPeludoBot.service;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface CommandManager {
    void handle(MessageReceivedEvent event);
}
