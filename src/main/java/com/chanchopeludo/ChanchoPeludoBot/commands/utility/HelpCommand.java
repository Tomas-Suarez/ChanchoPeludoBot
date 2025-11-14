package com.chanchopeludo.ChanchoPeludoBot.commands.utility;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.CommandConstants.HELP_TOTAL_PAGES;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildHelpEmbed;

@Component
public class HelpCommand implements Command {
    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("help", "Muestra la lista de comandos del bot.");
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        int currentPage = 1;

        MessageEmbed helpEmbed = buildHelpEmbed(
                event.getJDA().getSelfUser(),
                currentPage,
                HELP_TOTAL_PAGES
        );

        Button prevButton = Button.primary("help:prev:" + currentPage, "Anterior").withDisabled(true);
        Button nextButton = Button.primary("help:next:" + currentPage, "Siguiente")
                .withDisabled(currentPage >= HELP_TOTAL_PAGES);

        event.replyEmbeds(helpEmbed)
                .setComponents(ActionRow.of(prevButton, nextButton))
                .queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        int currentPage = 1;

        MessageEmbed helpEmbed = buildHelpEmbed(
                event.getJDA().getSelfUser(),
                currentPage,
                HELP_TOTAL_PAGES
        );

        Button prevButton = Button.primary("help:prev:" + currentPage, "Anterior").withDisabled(true);
        Button nextButton = Button.primary("help:next:" + currentPage, "Siguiente")
                .withDisabled(currentPage >= HELP_TOTAL_PAGES);

        event.getChannel().sendMessageEmbeds(helpEmbed)
                .setComponents(ActionRow.of(prevButton, nextButton))
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
}
