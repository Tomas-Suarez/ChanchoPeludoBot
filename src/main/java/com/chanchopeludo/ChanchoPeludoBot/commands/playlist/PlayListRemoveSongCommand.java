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
public class PlayListRemoveSongCommand implements Command {

    private final PlayListService playListService;

    public PlayListRemoveSongCommand(PlayListService playListService) {
        this.playListService = playListService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("playlist-remove", "Borra una canción de la playlist")
                .addOption(OptionType.STRING, "nombre", "Nombre de la playlist.")
                .addOption(OptionType.INTEGER, "posicion", "Posición de la canción");
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        String playListName = event.getOption("nombre").getAsString();
        int trackPosition = event.getOption("posicion").getAsInt();
        String guildId = event.getGuild().getId();
        String userId = event.getUser().getId();

        try {
            String removedTitleSong = playListService.removeTrack(playListName, trackPosition, guildId, userId);

            String msg = String.format(DESC_PLAYLIST_REMOVED, removedTitleSong, playListName);
            event.replyEmbeds(buildSuccessEmbed(TITLE_SONG_REMOVED, msg)).queue();

        }catch (Exception e){
            event.replyEmbeds(buildErrorEmbed("Error", e.getMessage())).setEphemeral(true).queue();
        }
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (args.size() < 2) {
            event.getChannel().sendMessageEmbeds(buildErrorEmbed(TITLE_ERROR_MISSING_ARGS, PLAYLIST_USAGE_REMOVE)).queue();
            return;
        }

        String guildId = event.getGuild().getId();
        String userId = event.getAuthor().getId();

        try{
            String lastArgs = args.get(args.size() - 1);
            int trackPosition = Integer.parseInt(lastArgs);

            String playListName = String.join(" ", args.subList(0, args.size() - 1));

            String removedTitleSong = playListService.removeTrack(playListName, trackPosition, guildId, userId);

            String msg = String.format(DESC_PLAYLIST_REMOVED, removedTitleSong, playListName);
            event.getChannel().sendMessageEmbeds(buildSuccessEmbed(TITLE_SONG_REMOVED, msg)).queue();
        }catch (Exception e){
            event.getChannel().sendMessageEmbeds(buildErrorEmbed("Error", e.getMessage())).queue();
        }
    }

    @Override
    public String getName() {
        return "playlist-remove";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("playlist-remove", "pl-remove");
    }
}
