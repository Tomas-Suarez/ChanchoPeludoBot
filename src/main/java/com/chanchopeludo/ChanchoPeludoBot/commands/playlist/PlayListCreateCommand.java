package com.chanchopeludo.ChanchoPeludoBot.commands.playlist;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
import com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.GenericConstants.TITLE_ERROR_MISSING_ARGS;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.*;

@Component
public class PlayListCreateCommand implements Command {

    private final PlayListService playListService;

    public PlayListCreateCommand(PlayListService playListService) {
        this.playListService = playListService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("playlist-create", "Crea una nueva playlist vacía.")
                .addOption(OptionType.STRING, "nombre", "El nombre para tu nueva playlist.", true);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {

        String playlistName = event.getOption("nombre").getAsString();
        String guildId = event.getGuild().getId();
        String creatorId = event.getUser().getId();

        MessageEmbed embed = handleCreatePlaylist(guildId, creatorId, playlistName);
        event.replyEmbeds(embed).queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {

        if (args.isEmpty()) {
            MessageEmbed embed = EmbedHelper.buildErrorEmbed(
                    TITLE_ERROR_MISSING_ARGS,
                    PLAYLIST_USAGE_CREATE);
            event.getChannel().sendMessageEmbeds(embed).queue();
            return;
        }

        String playlistName = String.join(" ", args);
        String guildId = event.getGuild().getId();
        String creatorId = event.getAuthor().getId();

        MessageEmbed embed = handleCreatePlaylist(guildId, creatorId, playlistName);
        event.getChannel().sendMessageEmbeds(embed).queue();
    }

    private MessageEmbed handleCreatePlaylist(String guildId, String creatorId, String playlistName) {
        try {
            playListService.createPlayList(playlistName, guildId, creatorId);

            return EmbedHelper.buildSuccessEmbed(
                    TITLE_PLAYLIST_CREATED,
                    String.format(DESC_PLAYLIST_CREATED, playlistName)
            );
        } catch (Exception e) {
            return EmbedHelper.buildErrorEmbed(TITLE_ERROR_PLAYLIST,
                    e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "playlist-create";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("playlist-create", "pl-create");
    }
}
