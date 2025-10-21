package com.chanchopeludo.ChanchoPeludoBot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public interface Command {

    /**
     *
     * @param event El evento del mensaje que disparó el comando.
     * @param args Los argumentos que el usuario escribió después del comando.
     */
    void handle(MessageReceivedEvent event, List<String> args);

    /**
     * Devuelve el nombre con el que se invoca el comando.
     * @return El nombre del comando (ej: "play", "perfil").
     */
    List<String> getNames();
}
