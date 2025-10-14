package com.chanchopeludo.ChanchoPeludoBot.commands.handlers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface InputHandler {

    boolean canHandle(String input);

    void handle(MessageReceivedEvent event, String input);
}
