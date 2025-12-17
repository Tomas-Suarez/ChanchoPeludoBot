package com.chanchopeludo.ChanchoPeludoBot.commands.playlist;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListItemEntity;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_NOT_IN_VOICE_CHANNEL;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildErrorEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildSuccessEmbed;

@Component
public class PlayListLoadCommand implements Command {

    private static final int DISCORD_MENU_LIMIT = 25;
    private final MusicService musicService;
    private final PlayListService playListService;

    public PlayListLoadCommand(MusicService musicService, PlayListService playListService) {
        this.musicService = musicService;
        this.playListService = playListService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("playlist-load", "Carga una playlist (local o pública).")
                .addOption(OptionType.STRING, "nombre", "Nombre de la playlist a cargar.", true);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        handleLoadCommand(event, event.getOption("nombre").getAsString(), event.getMember(), event.getGuild().getId());
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (args.isEmpty()) {
            sendMessage(event, buildErrorEmbed("Error", "Debes decirme el nombre de la playlist."), null, false);
            return;
        }
        handleLoadCommand(event, String.join(" ", args), event.getMember(), event.getGuild().getId());
    }

    private void handleLoadCommand(Object event, String playlistName, Member member, String guildId) {
        if (member == null || member.getVoiceState() == null || !member.getVoiceState().inAudioChannel()) {
            sendMessage(event, buildErrorEmbed("Error", MSG_NOT_IN_VOICE_CHANNEL), null, true);
            return;
        }

        try {
            List<PlayListEntity> candidates = playListService.searchPlaylists(playlistName, guildId);

            if (candidates.isEmpty()) {
                sendMessage(event, buildErrorEmbed("No encontrada", "No encontré ninguna playlist llamada '**" + playlistName), null, true);
                return;
            }

            if (candidates.size() == 1) {
                loadAndPlayById(event, candidates.get(0).getId(), member);
                return;
            }

            StringSelectMenu menu = buildSelectMenu(candidates);
            String msg = "🔍 Encontré varias playlists llamadas '**" + playlistName + "**'. ¿Cuál quieres?";
            sendMessage(event, null, menu, msg, true);

        } catch (Exception e) {
            sendMessage(event, buildErrorEmbed("Error", e.getMessage()), null, true);
        }
    }

    public void loadAndPlayById(Object event, Long playlistId, Member member) {
        try {
            PlayListEntity playlist = playListService.loadPlayListById(playlistId, member.getId());
            playMusic(playlist, member, event);

            String msg = "Cargando **" + playlist.getItems().size() + "** canciones de la playlist **" + playlist.getName() + "**.";

            sendMessage(event, buildSuccessEmbed("Playlist Cargada", msg), null, false);

        } catch (Exception e) {
            sendMessage(event, buildErrorEmbed("Error al cargar", e.getMessage()), null, true);
        }
    }

    private void playMusic(PlayListEntity playlist, Member member, Object event) {
        long voiceChannelId = member.getVoiceState().getChannel().getIdLong();
        long guildId = member.getGuild().getIdLong();
        long textChannelId = (event instanceof SlashCommandInteractionEvent slashEvent)
                ? slashEvent.getChannel().getIdLong()
                : ((MessageReceivedEvent) event).getChannel().getIdLong();

        for (PlayListItemEntity item : playlist.getItems()) {
            musicService.loadAndPlay(guildId, voiceChannelId, textChannelId, item.getTrack_Identifier());
        }
    }

    private StringSelectMenu buildSelectMenu(List<PlayListEntity> candidates) {
        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("pl-load-select")
                .setPlaceholder("Elige cuál playlist cargar...");

        candidates.stream()
                .limit(DISCORD_MENU_LIMIT)
                .forEach(pl -> {
                    String label = pl.getName() + " (" + pl.getServer().getGuild_name() + ")";
                    String description = "👤 " + pl.getCreator().getUsername() + " | 🎵 " + pl.getItems().size() + " canciones";
                    menuBuilder.addOption(label, String.valueOf(pl.getId()), description);
                });

        return menuBuilder.build();
    }

    private void sendMessage(Object event, MessageEmbed embed, StringSelectMenu menu, boolean ephemeral) {
        sendMessage(event, embed, menu, null, ephemeral);
    }

    private void sendMessage(Object event, MessageEmbed embed, StringSelectMenu menu, String textContent, boolean ephemeral) {
        if (event instanceof SlashCommandInteractionEvent slashEvent) {
            var reply = (textContent != null) ? slashEvent.reply(textContent) : slashEvent.replyEmbeds(embed);
            if (menu != null) reply.addActionRow(menu);
            reply.setEphemeral(ephemeral).queue();

        } else if (event instanceof MessageReceivedEvent msgEvent) {
            var channel = msgEvent.getChannel();
            var action = (textContent != null) ? channel.sendMessage(textContent) : channel.sendMessageEmbeds(embed);
            if (menu != null) action.setActionRow(menu);
            action.queue();
        }
    }

    @Override
    public String getName() {
        return "playlist-load";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("playlist-load", "pl-load");
    }
}