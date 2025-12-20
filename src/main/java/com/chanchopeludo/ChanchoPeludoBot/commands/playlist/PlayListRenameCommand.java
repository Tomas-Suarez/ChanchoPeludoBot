package com.chanchopeludo.ChanchoPeludoBot.commands.playlist;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
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
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildErrorEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildSuccessEmbed;

@Component
public class PlayListRenameCommand implements Command {

    private final PlayListService playListService;

    public PlayListRenameCommand(PlayListService playListService) {
        this.playListService = playListService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("playlist-rename", "Cambia el nombre de una playlist.")
                .addOption(OptionType.STRING, "nombre_actual", "Nombre actual de la playlist", true)
                .addOption(OptionType.STRING, "nombre_nuevo", "Nombre por el cual quieres cambiarlo", true);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        String oldPlayListName = event.getOption("nombre_actual").getAsString();
        String newPlayListName = event.getOption("nombre_nuevo").getAsString();
        String guildId = event.getGuild().getId();
        String userId = event.getUser().getId();

        try {
            playListService.renamePlayList(oldPlayListName, newPlayListName, guildId, userId);

            String msg = String.format(DESC_PLAYLIST_RENAMED, oldPlayListName, newPlayListName);

            event.replyEmbeds(buildSuccessEmbed(TITLE_PLAYLIST_RENAMED, msg)).queue();
        } catch (Exception e) {
            event.replyEmbeds(buildErrorEmbed("Error", e.getMessage())).setEphemeral(true).queue();
        }
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (args.size() < 2) {
            event.getChannel().sendMessageEmbeds(
                    buildErrorEmbed(TITLE_ERROR_MISSING_ARGS, PLAYLIST_USAGE_RENAME)
            ).queue();
            return;
        }

        String newPlayListName = args.get(args.size() - 1);
        String oldPlayListName = String.join(" ", args.subList(0, args.size() - 1));

        String guildId = event.getGuild().getId();
        String userId = event.getAuthor().getId();

        try {
            playListService.renamePlayList(oldPlayListName, newPlayListName, guildId, userId);

            String msg = String.format(DESC_PLAYLIST_RENAMED, oldPlayListName, newPlayListName);

            event.getChannel().sendMessageEmbeds(buildSuccessEmbed(TITLE_PLAYLIST_RENAMED, msg)).queue();
        } catch (Exception e) {
            event.getChannel().sendMessageEmbeds(buildErrorEmbed("Error", e.getMessage())).queue();
        }

    }

    @Override
    public String getName() {
        return "playlist-rename";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("playlist-rename", "pl-rename");
    }
}
