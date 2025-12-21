package com.chanchopeludo.ChanchoPeludoBot.commands.playlist;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.CommandConstants.ITEMS_PER_PAGE;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildErrorEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildPlayListListEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.PaginationHelper.*;

@Component
public class PlayListListsCommand implements Command {

    private final PlayListService playListService;

    public PlayListListsCommand(PlayListService playListService) {
        this.playListService = playListService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("playlist-list", "Muestra tus playlists propias o las publicas")
                .addOption(OptionType.STRING, "tipo", "Escribe 'all' para ver públicas o vacío para las tuyas.", false);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        String input = event.getOption("tipo") != null ? event.getOption("tipo").getAsString() : null;
        String scope = "all".equalsIgnoreCase(input) ? "all" : "mine";

        List<PlayListEntity> playlists = getPlaylistsByScope(scope, event.getGuild().getId(), event.getUser().getId());

        if (playlists.isEmpty()) {
            String msg = "all".equals(scope) ? "No hay playlists públicas." : "No tienes playlists creadas.";
            event.replyEmbeds(buildErrorEmbed("Sin Resultados", msg)).setEphemeral(true).queue();
            return;
        }

        int totalPages = calculateTotalPages(playlists.size(), ITEMS_PER_PAGE);
        MessageEmbed embed = buildPlayListListEmbed(playlists, 1, ITEMS_PER_PAGE, scope);

        List<Button> buttons = createPaginationButtons("playlist-list", 1, totalPages, scope);

        event.replyEmbeds(embed).setActionRow(buttons).queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        String input = !args.isEmpty() ? args.get(0) : null;
        String scope = "all".equalsIgnoreCase(input) ? "all" : "mine";

        List<PlayListEntity> playlists = getPlaylistsByScope(scope, event.getGuild().getId(), event.getAuthor().getId());

        if (playlists.isEmpty()) {
            String msg = "all".equals(scope) ? "No hay playlists públicas." : "No tienes playlists creadas.";
            event.getChannel().sendMessageEmbeds(buildErrorEmbed("Sin Resultados", msg)).queue();
            return;
        }

        int totalPages = calculateTotalPages(playlists.size(), ITEMS_PER_PAGE);
        MessageEmbed embed = buildPlayListListEmbed(playlists, 1, ITEMS_PER_PAGE, scope);

        List<Button> buttons = createPaginationButtons("playlist-list", 1, totalPages, scope);

        event.getChannel().sendMessageEmbeds(embed).setActionRow(buttons).queue();
    }

    @Override
    public String getName() {
        return "playlist-list";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("playlist-list", "pl-list");
    }

    @Override
    public boolean handlesButton(String componentId) {
        return componentId.startsWith("playlist-list" + ":");
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] idParts = event.getComponentId().split(":");
        String action = idParts[1];
        int currentPage = Integer.parseInt(idParts[2]);
        String scope = idParts[3];

        List<PlayListEntity> playlists = getPlaylistsByScope(scope, event.getGuild().getId(), event.getUser().getId());

        int totalPages = calculateTotalPages(playlists.size(), ITEMS_PER_PAGE);
        int newPage = calculateNewPage(action, currentPage, totalPages);

        MessageEmbed newEmbed = buildPlayListListEmbed(playlists, newPage, ITEMS_PER_PAGE, scope);
        List<Button> buttons = createPaginationButtons("playlist-list", newPage, totalPages, scope);

        event.editMessageEmbeds(newEmbed)
                .setComponents(ActionRow.of(buttons))
                .queue();
    }

    private List<PlayListEntity> getPlaylistsByScope(String scope, String guildId, String userId) {
        if ("all".equals(scope)) {
            return playListService.getPublicPlayLists(guildId);
        } else {
            return playListService.getUserPlayLists(userId, guildId);
        }
    }
}
