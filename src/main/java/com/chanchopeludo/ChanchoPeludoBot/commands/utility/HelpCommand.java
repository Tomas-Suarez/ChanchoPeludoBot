package com.chanchopeludo.ChanchoPeludoBot.commands.utility;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.CommandConstants.HELP_TOTAL_PAGES;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.PaginationHelper.calculateNewPage;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.PaginationHelper.createPaginationButtons;

@Component
public class HelpCommand implements Command {

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("help", "Muestra la lista de comandos del bot.");
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        int currentPage = 1;

        MessageEmbed helpEmbed = EmbedHelper.buildHelpEmbed(
                event.getJDA().getSelfUser(),
                currentPage,
                HELP_TOTAL_PAGES
        );

        List<Button> buttons = createPaginationButtons("help", currentPage, HELP_TOTAL_PAGES);

        event.replyEmbeds(helpEmbed)
                .setComponents(ActionRow.of(buttons))
                .queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        int currentPage = 1;

        MessageEmbed helpEmbed = EmbedHelper.buildHelpEmbed(
                event.getJDA().getSelfUser(),
                currentPage,
                HELP_TOTAL_PAGES
        );

        List<Button> buttons = createPaginationButtons("help", currentPage, HELP_TOTAL_PAGES);

        event.getChannel().sendMessageEmbeds(helpEmbed)
                .setComponents(ActionRow.of(buttons))
                .queue();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("help");
    }

    @Override
    public boolean handlesButton(String componentId) {
        return componentId.startsWith("help:");
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] idParts = event.getComponentId().split(":");
        String action = idParts[1];
        int currentPage = Integer.parseInt(idParts[2]);

        int newPage = calculateNewPage(action, currentPage, HELP_TOTAL_PAGES);

        MessageEmbed helpEmbed = EmbedHelper.buildHelpEmbed(
                event.getJDA().getSelfUser(),
                newPage,
                HELP_TOTAL_PAGES
        );

        List<Button> buttons = createPaginationButtons("help", newPage, HELP_TOTAL_PAGES);

        event.editMessageEmbeds(helpEmbed)
                .setComponents(ActionRow.of(buttons))
                .queue();
    }
}