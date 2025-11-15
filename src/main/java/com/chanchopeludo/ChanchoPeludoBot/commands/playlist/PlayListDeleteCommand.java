package com.chanchopeludo.ChanchoPeludoBot.commands.playlist;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.GenericConstants.TITLE_ERROR_MISSING_ARGS;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.*;

@Component
public class PlayListDeleteCommand implements Command {

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("playlist-delete", "Borra una playlist de forma permanente.")
                .addOption(OptionType.STRING, "nombre", "El nombre de tu playlist.", true);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        String playlistName = event.getOption("nombre").getAsString();

        MessageEmbed embed = EmbedHelper.buildErrorEmbed("⚠️ Confirmación Requerida",
                "¿Estás seguro de que quieres borrar la playlist **" + playlistName + "**?\nEsta acción es irreversible.");

        Button confirmButton = Button.danger("pl-delete:confirm:" + playlistName, "Sí, borrar");
        Button cancelButton = Button.secondary("pl-delete:cancel", "Cancelar");

        event.replyEmbeds(embed)
                .setComponents(ActionRow.of(confirmButton, cancelButton))
                .setEphemeral(true)
                .queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (args.isEmpty()) {
            MessageEmbed embed = EmbedHelper.buildErrorEmbed(TITLE_ERROR_MISSING_ARGS, PLAYLIST_USAGE_DELETE);
            event.getChannel().sendMessageEmbeds(embed).queue();
            return;
        }

        String playlistName = String.join(" ", args);

        MessageEmbed embed = EmbedHelper.buildErrorEmbed("Confirmación Requerida",
                "¿Estás seguro de que quieres borrar la playlist **" + playlistName + "**?\nEsta acción es irreversible.");

        Button confirmButton = Button.danger("pl-delete:confirm:" + playlistName, "Sí, borrar");
        Button cancelButton = Button.secondary("pl-delete:cancel", "Cancelar");

        event.getChannel().sendMessageEmbeds(embed)
                .setComponents(ActionRow.of(confirmButton, cancelButton))
                .queue();
    }

    @Override
    public String getName() {
        return "playlist-delete";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("playlist-delete", "pl-delete");
    }
}