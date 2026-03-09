package com.chanchopeludo.ChanchoPeludoBot.commands.playlist;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
import com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.CommandConstants.ITEMS_PER_PAGE;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.GenericConstants.TITLE_ERROR_MISSING_ARGS;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.PLAYLIST_USAGE_VIEW;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildErrorEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildPlaylistViewEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.PaginationHelper.*;

@Component
public class PlayListViewCommand implements Command {

    private final PlayListService playListService;

    public PlayListViewCommand(PlayListService playListService) {
        this.playListService = playListService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("playlist-view", "Muestra las canciones de una playlist.")
                .addOption(OptionType.STRING, "nombre", "Nombre de la playlist a ver.", true);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        String playlistName = event.getOption("nombre").getAsString();
        String guildId = event.getGuild().getId();
        String userId = event.getUser().getId();

        try {
            List<PlayListEntity> results = playListService.viewPlayList(playlistName, guildId, userId);

            if (results.isEmpty()) {
                event.replyEmbeds(buildErrorEmbed("No encontrada", "No encontré ninguna playlist llamada '**" + playlistName + "**'."))
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (results.size() == 1) {
                PlayListEntity playlist = results.get(0);
                MessageEmbed embed = buildPlaylistViewEmbed(playlist, 1);

                int totalPages = calculateTotalPages(playlist.getItems().size(), ITEMS_PER_PAGE);
                List<Button> buttons = createPaginationButtons("pl-view", 1, totalPages, String.valueOf(playlist.getId()));

                event.replyEmbeds(embed).setComponents(ActionRow.of(buttons)).queue();
            }
            else {
                StringSelectMenu menu = createDisambiguationMenu(playlistName, results);
                String msg = "🔍 Encontré varias playlists llamadas '**" + playlistName + "**'. ¿Cuál quieres ver?";

                event.reply(msg).setComponents(ActionRow.of(menu)).setEphemeral(true).queue();
            }

        } catch (Exception e) {
            event.replyEmbeds(buildErrorEmbed("Error", e.getMessage())).setEphemeral(true).queue();
        }
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (args.isEmpty()) {
            event.getChannel().sendMessageEmbeds(buildErrorEmbed(TITLE_ERROR_MISSING_ARGS, PLAYLIST_USAGE_VIEW)).queue();
            return;
        }

        String playlistName = String.join(" ", args);
        String guildId = event.getGuild().getId();
        String userId = event.getAuthor().getId();

        try {
            List<PlayListEntity> results = playListService.viewPlayList(playlistName, guildId, userId);

            if (results.isEmpty()) {
                event.getChannel().sendMessageEmbeds(buildErrorEmbed("No encontrada", "No encontré ninguna playlist llamada '**" + playlistName + "**'.")).queue();
                return;
            }

            if (results.size() == 1) {
                PlayListEntity playlist = results.get(0);
                MessageEmbed embed = EmbedHelper.buildPlaylistViewEmbed(playlist, 1);

                int totalPages = calculateTotalPages(playlist.getItems().size(), ITEMS_PER_PAGE);
                List<Button> buttons = createPaginationButtons("pl-view", 1, totalPages, String.valueOf(playlist.getId()));

                event.getChannel().sendMessageEmbeds(embed).setComponents(ActionRow.of(buttons)).queue();
            }
            else {
                StringSelectMenu menu = createDisambiguationMenu(playlistName, results);
                String msg = "🔍 Encontré varias playlists llamadas '**" + playlistName + "**'. ¿Cuál quieres ver?";

                event.getChannel().sendMessage(msg).setComponents(ActionRow.of(menu)).queue();
            }

        } catch (Exception e) {
            event.getChannel().sendMessageEmbeds(buildErrorEmbed("Error", e.getMessage())).queue();
        }
    }

    private StringSelectMenu createDisambiguationMenu(String name, List<PlayListEntity> candidates) {
        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("pl-view-select")
                .setPlaceholder("Elige cuál playlist ver...");

        for (PlayListEntity pl : candidates) {
            String label = pl.getName();
            String description = "👤 " + pl.getCreator().getUsername() + " | 🎵 " + pl.getItems().size() + " canciones";
            menuBuilder.addOption(label, String.valueOf(pl.getId()), description);
        }

        return menuBuilder.build();
    }

    @Override
    public String getName() {
        return "playlist-view";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("playlist-view", "pl-view");
    }

    @Override
    public boolean handlesMenu(String componentId) {
        return componentId.equals("pl-view-select");
    }

    @Override
    public void onMenuInteraction(StringSelectInteractionEvent event) {
        String selectedIdStr = event.getValues().get(0);

        try {
            Long playlistId = Long.parseLong(selectedIdStr);
            PlayListEntity playlist = playListService.loadPlayListById(playlistId, event.getUser().getId());

            int totalPages = calculateTotalPages(playlist.getItems().size(), ITEMS_PER_PAGE);
            MessageEmbed embed = buildPlaylistViewEmbed(playlist, 1);

            List<Button> buttons = createPaginationButtons("pl-view", 1, totalPages, selectedIdStr);

            event.editMessageEmbeds(embed)
                    .setContent("")
                    .setComponents(ActionRow.of(buttons))
                    .queue();

        } catch (Exception e) {
            event.reply("Error: " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    public boolean handlesButton(String componentId) {
        return componentId.startsWith("pl-view:");
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] idParts = event.getComponentId().split(":", 4);
        String action = idParts[1];
        int currentPage = Integer.parseInt(idParts[2]);
        String playlistIdStr = idParts[3];

        try {
            Long playlistId = Long.parseLong(playlistIdStr);
            PlayListEntity playlist = playListService.loadPlayListById(playlistId, event.getUser().getId());

            int totalPages = calculateTotalPages(playlist.getItems().size(), ITEMS_PER_PAGE);
            int newPage = calculateNewPage(action, currentPage, totalPages);

            MessageEmbed newEmbed = buildPlaylistViewEmbed(playlist, newPage);
            List<Button> buttons = createPaginationButtons("pl-view", newPage, totalPages, playlistIdStr);

            event.editMessageEmbeds(newEmbed)
                    .setComponents(ActionRow.of(buttons))
                    .queue();

        } catch (Exception e) {
            event.reply("Error: " + e.getMessage()).setEphemeral(true).queue();
        }
    }
}