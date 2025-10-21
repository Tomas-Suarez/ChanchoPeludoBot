package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.service.CommandManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.AppConstants.DEFAULT_PREFIX;

@Service
public class CommandManagerImpl implements CommandManager {

    private final Map<String, Command> commandMap;

    public CommandManagerImpl(List<Command> commandList) {
        this.commandMap = new HashMap<>();

        for (Command command : commandList) {
            for (String name : command.getNames()) {
                this.commandMap.put(name.toLowerCase(), command);
            }
        }
    }

    @Override
    public void handle(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw().substring(DEFAULT_PREFIX.length()).trim();

        String[] parts = content.split("\\s+");
        String commandName = parts[0].toLowerCase();

        Command command = this.commandMap.get(commandName);

        if (command != null) {
            List<String> args = Arrays.asList(parts).subList(1, parts.length);
            command.handle(event, args);
        }
    }
}