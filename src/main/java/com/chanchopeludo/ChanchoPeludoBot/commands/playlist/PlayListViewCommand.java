package com.chanchopeludo.ChanchoPeludoBot.commands.playlist;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
import com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.GenericConstants.TITLE_ERROR_MISSING_ARGS;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.PLAYLIST_USAGE_VIEW;

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

        try {
            PlayListEntity playlist = playListService.viewPlayList(playlistName, guildId);
            sendEmbedWithPagination(event, playlist);
        } catch (Exception e) {
            event.replyEmbeds(EmbedHelper.buildErrorEmbed("Error", e.getMessage())).setEphemeral(true).queue();
        }
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (args.isEmpty()) {
            MessageEmbed embed = EmbedHelper.buildErrorEmbed(TITLE_ERROR_MISSING_ARGS, PLAYLIST_USAGE_VIEW);
            event.getChannel().sendMessageEmbeds(embed).queue();
            return;
        }

        String playlistName = String.join(" ", args);
        String guildId = event.getGuild().getId();

        try {
            PlayListEntity playlist = playListService.viewPlayList(playlistName, guildId);
            sendEmbedWithPagination(event, playlist);
        } catch (Exception e) {
            event.getChannel().sendMessageEmbeds(EmbedHelper.buildErrorEmbed("Error", e.getMessage())).queue();
        }
    }

    private void sendEmbedWithPagination(Object event, PlayListEntity playlist) {
        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil((double) playlist.getItems().size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;

        MessageEmbed embed = EmbedHelper.buildPlaylistViewEmbed(playlist, 1);

        Button prevButton = Button.primary("pl-view:prev:1:" + playlist.getName(), "Anterior").withDisabled(true);
        Button nextButton = Button.primary("pl-view:next:1:" + playlist.getName(), "Siguiente").withDisabled(totalPages <= 1);

        if (event instanceof SlashCommandInteractionEvent) {
            ((SlashCommandInteractionEvent) event).replyEmbeds(embed)
                    .setActionRow(prevButton, nextButton)
                    .queue();
        } else if (event instanceof MessageReceivedEvent) {
            ((MessageReceivedEvent) event).getChannel().sendMessageEmbeds(embed)
                    .setActionRow(prevButton, nextButton)
                    .queue();
        }
    }

    @Override
    public String getName() {
        return "playlist-view";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("playlist-view", "pl-view");
    }
}