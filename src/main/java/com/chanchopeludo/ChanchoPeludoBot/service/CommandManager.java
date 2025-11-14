package com.chanchopeludo.ChanchoPeludoBot.service;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public interface CommandManager {
    void handle(MessageReceivedEvent event);
    void handleSlash(SlashCommandInteractionEvent event);
    List<CommandData> getAllSlashCommandData();
}

