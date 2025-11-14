package com.chanchopeludo.ChanchoPeludoBot.config;

import com.chanchopeludo.ChanchoPeludoBot.service.CommandManager;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CommandRegistration {
    private static final Logger log = LoggerFactory.getLogger(CommandRegistration.class);

    private final JDA jda;
    private final CommandManager commandManager;

    public CommandRegistration(JDA jda, CommandManager commandManager) {
        this.jda = jda;
        this.commandManager = commandManager;
    }

    @PostConstruct
    public void registerCommands() {
        List<CommandData> commandDataList = commandManager.getAllSlashCommandData();

        if (commandDataList.isEmpty()) {
            log.warn("No se encontraron comandos para registrar.");
            return;
        }

        log.info("Registrando {} comandos slash en Discord...", commandDataList.size());

        jda.updateCommands().addCommands(commandDataList).queue(
                (success) -> log.info("¡Comandos registrados exitosamente!"),
                (error) -> log.error("Error al registrar comandos: {}", error.getMessage(), error)
        );

    }
}
