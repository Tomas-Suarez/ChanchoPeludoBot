package com.chanchopeludo.ChanchoPeludoBot.listeners;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.CustomException;
import com.chanchopeludo.ChanchoPeludoBot.service.CommandManager;
import com.chanchopeludo.ChanchoPeludoBot.service.UserService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.AppConstants.DEFAULT_PREFIX;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.XpConstants.XP_PER_MESSAGE;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildErrorEmbed;

@Component
public class DiscordCommandListener extends ListenerAdapter {

    private final JDA jda;
    private final CommandManager commandManager;
    private final UserService userService;
    private final List<Command> commands;

    public DiscordCommandListener(JDA jda, CommandManager commandManager, UserService userService, List<Command> commands) {
        this.jda = jda;
        this.commandManager = commandManager;
        this.userService = userService;
        this.commands = commands;
    }

    @PostConstruct
    public void register() {
        jda.addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            commandManager.handleSlash(event);
        } catch (CustomException e) {
            event.replyEmbeds(buildErrorEmbed("Aviso", e.getMessage()))
                    .setEphemeral(true).queue();
        } catch (Exception e) {
            event.replyEmbeds(buildErrorEmbed("Error", "Ocurrió un error inesperado."))
                    .setEphemeral(true).queue();
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getMessage().getContentRaw().startsWith(DEFAULT_PREFIX)) {
            return;
        }

        try {
            String userId = event.getAuthor().getId();
            String username = event.getAuthor().getName();
            String serverId = event.getGuild().getId();
            String serverName = event.getGuild().getName();

            userService.addExp(userId, username, serverId, serverName, XP_PER_MESSAGE);

            commandManager.handle(event);

        } catch (CustomException e) {
            event.getChannel().sendMessageEmbeds(buildErrorEmbed("Aviso", e.getMessage())).queue();

        } catch (Exception e) {
            event.getChannel().sendMessageEmbeds(buildErrorEmbed("Error", "Ocurrió un error inesperado.")).queue();
            e.printStackTrace();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        try {
            String componentId = event.getComponentId();
            for (Command cmd : commands) {
                if (cmd.handlesButton(componentId)) {
                    cmd.onButtonInteraction(event);
                    return;
                }
            }
        } catch (CustomException e) {
            event.replyEmbeds(buildErrorEmbed("Aviso", e.getMessage())).setEphemeral(true).queue();
        } catch (Exception e) {
            event.replyEmbeds(buildErrorEmbed("Error", "Error al procesar botón.")).setEphemeral(true).queue();
            e.printStackTrace();
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        try {
            String componentId = event.getComponentId();
            for (Command cmd : commands) {
                if (cmd.handlesMenu(componentId)) {
                    cmd.onMenuInteraction(event);
                    return;
                }
            }
        } catch (CustomException e) {
            event.replyEmbeds(buildErrorEmbed("Aviso", e.getMessage())).setEphemeral(true).queue();
        } catch (Exception e) {
            event.replyEmbeds(buildErrorEmbed("Error", "Error al procesar menú.")).setEphemeral(true).queue();
            e.printStackTrace();
        }
    }
}