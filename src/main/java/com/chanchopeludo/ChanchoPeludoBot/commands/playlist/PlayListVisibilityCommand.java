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
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.GenericConstants.TITLE_INVALID_ARGS;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.*;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildErrorEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildSuccessEmbed;

@Component
public class PlayListVisibilityCommand implements Command {

    private final PlayListService playListService;

    public PlayListVisibilityCommand(PlayListService playListService) {
        this.playListService = playListService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("playlist-public", "Cambia la visibilidad de una playlist (Pública/Privada).")
                .addOption(OptionType.STRING, "nombre", "Nombre de la playlist.", true)
                .addOption(OptionType.BOOLEAN, "es_publica", "True para pública, False para privada.", true);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        String playlistName = event.getOption("nombre").getAsString();
        boolean isPublic = event.getOption("es_publica").getAsBoolean();
        String guildId = event.getGuild().getId();
        String userId = event.getUser().getId();

        try{

            playListService.updateVisibility(playlistName, isPublic, guildId, userId);

            String estado = isPublic ? "Pública" : "Privada";
            String msg = String.format(DESC_PLAYLIST_VISIBILITY, playlistName, estado);

            event.replyEmbeds(buildSuccessEmbed(TITLE_PLAYLIST_VISIBILITY, msg)).queue();

        }catch (Exception e) {
            event.replyEmbeds(buildErrorEmbed("Error", e.getMessage())).setEphemeral(true).queue();
        }
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (args.size() < 2) {
            event.getChannel().sendMessageEmbeds(
                    buildErrorEmbed(TITLE_ERROR_MISSING_ARGS, PLAYLIST_USAGE_VISIBILITY)
            ).queue();
            return;
        }

        String lastArg = args.get(args.size() - 1).toLowerCase();
        boolean isPublic;

        if (lastArg.equals("true") || lastArg.equals("public")) {
            isPublic = true;
        } else if (lastArg.equals("false") || lastArg.equals("private")) {
            isPublic = false;
        } else {
            event.getChannel().sendMessageEmbeds(
                    buildErrorEmbed(TITLE_INVALID_ARGS, "El último valor debe ser `true` (públic) o `false` (private).")
            ).queue();
            return;
        }

        String playlistName = String.join(" ", args.subList(0, args.size() - 1));

        String guildId = event.getGuild().getId();
        String userId = event.getAuthor().getId();

        try {
            playListService.updateVisibility(playlistName, isPublic, guildId, userId);

            String estado = isPublic ? "Pública" : "Privada";
            String msg = String.format(DESC_PLAYLIST_VISIBILITY, playlistName, estado);

            event.getChannel().sendMessageEmbeds(buildSuccessEmbed(TITLE_PLAYLIST_VISIBILITY, msg)).queue();

        } catch (Exception e) {
            event.getChannel().sendMessageEmbeds(buildErrorEmbed("Error", e.getMessage())).queue();
        }
    }

    @Override
    public String getName() {
        return "playlist-public";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("playlist-public", "pl-public");
    }
}
