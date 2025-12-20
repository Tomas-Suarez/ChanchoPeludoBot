package com.chanchopeludo.ChanchoPeludoBot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public interface Command {

    /**
     * Define la estructura del comando de barra (/) para JDA.
     * (Descripción, opciones, etc.)
     *
     * @return Un objeto CommandData con la definición.
     */
    CommandData getSlashCommandData();

    /**
     * Lógica para ejecutar el comando cuando es invocado por un Slash Command (/).
     *
     * @param event El evento de la interacción.
     */
    void executeSlash(SlashCommandInteractionEvent event);

    /**
     * Lógica para ejecutar el comando cuando es invocado por un Mensaje de Texto (c!).
     *
     * @param event El evento del mensaje que disparó el comando.
     * @param args  La lista de argumentos que el usuario escribió después del comando.
     */
    void executeText(MessageReceivedEvent event, List<String> args);

    /**
     * Devuelve el nombre principal con el que se invoca el comando.
     *
     * @return El nombre del comando (ej. "play", "perfil").
     */
    String getName();

    /**
     * Devuelve el nombre con el que se invoca un comando.
     *
     * @return El nombre del comando (ej. "play", "p").
     */
    List<String> getTextNames();

    /**
     * Determina si este comando es responsable de manejar un botón específico.
     * <p>
     * Se debe sobrescribir en el comando si este genera botones.
     *
     * @param componentId El ID del botón presionado (ej: "queue:next:1").
     * @return true si este comando debe manejar la acción, false en caso contrario.
     */
    default boolean handlesButton(String componentId) {
        return false;
    }

    /**
     * Ejecuta la lógica correspondiente cuando se presiona un botón manejado por este comando.
     * <p>
     * Solo se llamará si {@link #handlesButton(String)} devuelve true.
     *
     * @param event El evento de interacción del botón.
     */
    default void onButtonInteraction(ButtonInteractionEvent event) {
        // Por defecto no hace nada.
    }

    /**
     * Determina si este comando es responsable de manejar un menú de selección específico.
     * <p>
     * Se debe sobrescribir en el comando si este genera menús desplegables.
     *
     * @param componentId El ID del menú (ej: "pl-load-select").
     * @return true si este comando debe manejar la acción, false en caso contrario.
     */
    default boolean handlesMenu(String componentId) {
        return false;
    }

    /**
     * Ejecuta la lógica correspondiente cuando se selecciona una opción en un menú manejado por este comando.
     * <p>
     * Solo se llamará si {@link #handlesMenu(String)} devuelve true.
     *
     * @param event El evento de interacción del menú.
     */
    default void onMenuInteraction(StringSelectInteractionEvent event) {
    }
}
