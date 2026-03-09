package com.chanchopeludo.ChanchoPeludoBot.commands.playlist;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListItemEntity;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_NOT_IN_VOICE_CHANNEL;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.PLAYLIST_USAGE_LOAD;
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
        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || !member.getVoiceState().inAudioChannel()) {
            event.replyEmbeds(buildErrorEmbed("Error", MSG_NOT_IN_VOICE_CHANNEL)).setEphemeral(true).queue();
            return;
        }

        String playlistName = event.getOption("nombre").getAsString();
        String guildId = event.getGuild().getId();
        String userId = event.getUser().getId();

        try {
            List<PlayListEntity> candidates = playListService.viewPlayList(playlistName, guildId, userId);

            if (candidates.isEmpty()) {
                event.replyEmbeds(buildErrorEmbed("No encontrada", "No encontré ninguna playlist llamada '**" + playlistName + "**'.")).setEphemeral(true).queue();
                return;
            }

            if (candidates.size() == 1) {
                PlayListEntity pl = candidates.get(0);
                MessageEmbed embed = loadAndPlay(pl, member, event.getChannel());
                event.replyEmbeds(embed).queue();
            } else {
                StringSelectMenu menu = buildSelectMenu(candidates);
                event.reply("🔍 Encontré varias playlists llamadas '**" + playlistName + "**'. ¿Cuál quieres cargar?")
                        .setComponents(ActionRow.of(menu))
                        .setEphemeral(true)
                        .queue();
            }

        } catch (Exception e) {
            event.replyEmbeds(buildErrorEmbed("Error", e.getMessage())).setEphemeral(true).queue();
        }
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (args.isEmpty()) {
            event.getChannel().sendMessageEmbeds(buildErrorEmbed("Error", PLAYLIST_USAGE_LOAD)).queue();
            return;
        }

        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || !member.getVoiceState().inAudioChannel()) {
            event.getChannel().sendMessageEmbeds(buildErrorEmbed("Error", MSG_NOT_IN_VOICE_CHANNEL)).queue();
            return;
        }

        String playlistName = String.join(" ", args);
        String guildId = event.getGuild().getId();
        String userId = event.getAuthor().getId();

        try {
            List<PlayListEntity> candidates = playListService.viewPlayList(playlistName, guildId, userId);

            if (candidates.isEmpty()) {
                event.getChannel().sendMessageEmbeds(buildErrorEmbed("No encontrada", "No encontré ninguna playlist llamada '**" + playlistName + "**'.")).queue();
                return;
            }

            if (candidates.size() == 1) {
                PlayListEntity pl = candidates.get(0);
                MessageEmbed embed = loadAndPlay(pl, member, event.getChannel());
                event.getChannel().sendMessageEmbeds(embed).queue();
            } else {
                StringSelectMenu menu = buildSelectMenu(candidates);
                event.getChannel().sendMessage("🔍 Encontré varias playlists llamadas '**" + playlistName + "**'. ¿Cuál quieres cargar?")
                        .setComponents(ActionRow.of(menu))
                        .queue();
            }

        } catch (Exception e) {
            event.getChannel().sendMessageEmbeds(buildErrorEmbed("Error", e.getMessage())).queue();
        }
    }

    @Override
    public boolean handlesMenu(String componentId) {
        return componentId.equals("pl-load-select");
    }

    @Override
    public void onMenuInteraction(StringSelectInteractionEvent event) {
        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || !member.getVoiceState().inAudioChannel()) {
            event.reply(MSG_NOT_IN_VOICE_CHANNEL).setEphemeral(true).queue();
            return;
        }

        String selectedIdStr = event.getValues().get(0);

        try {
            Long playlistId = Long.parseLong(selectedIdStr);
            PlayListEntity playlist = playListService.loadPlayListById(playlistId, event.getUser().getId());

            MessageEmbed embed = loadAndPlay(playlist, member, event.getChannel());

            event.editMessageEmbeds(embed)
                    .setContent("")
                    .setComponents()
                    .queue();

        } catch (Exception e) {
            event.reply("Error al cargar: " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    private MessageEmbed loadAndPlay(PlayListEntity playlist, Member member, MessageChannel channel) {
        if (playlist.getItems().isEmpty()) {
            return buildErrorEmbed("Playlist Vacía", "La playlist **" + playlist.getName() + "** no tiene canciones.");
        }

        long voiceChannelId = member.getVoiceState().getChannel().getIdLong();
        long guildId = member.getGuild().getIdLong();
        long textChannelId = channel.getIdLong();

        for (PlayListItemEntity item : playlist.getItems()) {
            musicService.loadAndPlay(guildId, voiceChannelId, textChannelId, item.getTrack_Identifier());
        }

        String msg = "Cargando **" + playlist.getItems().size() + "** canciones de la playlist **" + playlist.getName() + "**.";
        return buildSuccessEmbed("Playlist Cargada", msg);
    }

    private StringSelectMenu buildSelectMenu(List<PlayListEntity> candidates) {
        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("pl-load-select")
                .setPlaceholder("Elige cuál playlist cargar...");

        candidates.stream()
                .limit(DISCORD_MENU_LIMIT)
                .forEach(pl -> {
                    String label = pl.getName();
                    String description = "👤 " + pl.getCreator().getUsername() + " | 🎵 " + pl.getItems().size() + " canciones";
                    menuBuilder.addOption(label, String.valueOf(pl.getId()), description);
                });

        return menuBuilder.build();
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