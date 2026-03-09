package com.chanchopeludo.ChanchoPeludoBot.commands.playlist;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
import com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.GenericConstants.TITLE_ERROR_MISSING_ARGS;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.DESC_PLAYLIST_DELETED;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.PLAYLIST_USAGE_DELETE;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.TITLE_ERROR_PLAYLIST;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.TITLE_PLAYLIST_DELETED;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildErrorEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildSuccessEmbed;

@Component
public class PlayListDeleteCommand implements Command {

    private final PlayListService playListService;

    public PlayListDeleteCommand(PlayListService playListService) {
        this.playListService = playListService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("playlist-delete", "Borra una playlist de forma permanente.")
                .addOption(OptionType.STRING, "nombre", "El nombre de tu playlist.", true);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        String playlistName = event.getOption("nombre").getAsString();

        MessageEmbed embed = createConfirmEmbed(playlistName);
        List<Button> buttons = createConfirmButtons(playlistName);

        event.replyEmbeds(embed)
                .setComponents(ActionRow.of(buttons))
                .setEphemeral(true)
                .queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (args.isEmpty()) {
            event.getChannel().sendMessageEmbeds(EmbedHelper.buildErrorEmbed(TITLE_ERROR_MISSING_ARGS, PLAYLIST_USAGE_DELETE)).queue();
            return;
        }

        String playlistName = String.join(" ", args);

        MessageEmbed embed = createConfirmEmbed(playlistName);
        List<Button> buttons = createConfirmButtons(playlistName);

        event.getChannel().sendMessageEmbeds(embed)
                .setComponents(ActionRow.of(buttons))
                .queue();
    }

    @Override
    public boolean handlesButton(String componentId) {
        return componentId.startsWith("pl-delete");
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] idParts = event.getComponentId().split(":", 3);
        String action = idParts[1];

        if (!event.getMessage().getComponents().isEmpty()) {
            ActionRow row = (ActionRow) event.getMessage().getComponents().get(0);
            event.editComponents(row.withDisabled(true)).queue();
        }

        if ("cancel".equals(action)) {
            MessageEmbed embed = buildSuccessEmbed("Operación Cancelada", "No se borró ninguna playlist.");
            event.getHook().editOriginalEmbeds(embed).queue();
            return;
        }

        if ("confirm".equals(action)) {
            String playlistName = idParts[2];
            String guildId = event.getGuild().getId();
            String userId = event.getUser().getId();

            try {
                playListService.deletePlayList(playlistName, guildId, userId);

                MessageEmbed embed = buildSuccessEmbed(
                        TITLE_PLAYLIST_DELETED,
                        String.format(DESC_PLAYLIST_DELETED, playlistName)
                );
                event.getHook().editOriginalEmbeds(embed).queue();

            } catch (Exception e) {
                MessageEmbed embed = buildErrorEmbed(TITLE_ERROR_PLAYLIST, e.getMessage());
                event.getHook().editOriginalEmbeds(embed).queue();
            }
        }
    }

    private MessageEmbed createConfirmEmbed(String playlistName) {
        return buildErrorEmbed("⚠️ Confirmación Requerida",
                "¿Estás seguro de que quieres borrar la playlist **" + playlistName + "**?\nEsta acción es irreversible.");
    }

    private List<Button> createConfirmButtons(String playlistName) {
        return Arrays.asList(
                Button.danger("pl-delete:confirm:" + playlistName, "Sí, borrar"),
                Button.secondary("pl-delete:cancel", "Cancelar")
        );
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