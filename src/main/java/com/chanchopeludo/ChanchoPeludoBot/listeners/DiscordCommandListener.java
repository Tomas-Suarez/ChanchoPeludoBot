package com.chanchopeludo.ChanchoPeludoBot.listeners;

import com.chanchopeludo.ChanchoPeludoBot.dto.QueueState;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListItemEntity;
import com.chanchopeludo.ChanchoPeludoBot.service.CommandManager;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.PlayListService;
import com.chanchopeludo.ChanchoPeludoBot.service.UserService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.AppConstants.DEFAULT_PREFIX;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.PlayListConstants.*;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.XpConstants.XP_PER_MESSAGE;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.*;

@Component
public class DiscordCommandListener extends ListenerAdapter {

    private final JDA jda;
    private final CommandManager commandManager;
    private final MusicService musicService;
    private final UserService userService;
    private final PlayListService playListService;

    public DiscordCommandListener(JDA jda, CommandManager commandManager, MusicService musicService, UserService userService, PlayListService playListService) {
        this.jda = jda;
        this.commandManager = commandManager;
        this.musicService = musicService;
        this.userService = userService;
        this.playListService = playListService;
    }

    @PostConstruct
    public void register() {
        jda.addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        commandManager.handleSlash(event);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getMessage().getContentRaw().startsWith(DEFAULT_PREFIX)) {
            return;
        }

        String userId = event.getAuthor().getId();
        String username = event.getAuthor().getName();

        String serverId = event.getGuild().getId();
        String serverName = event.getGuild().getName();

        userService.addExp(userId, username, serverId, serverName, XP_PER_MESSAGE);

        commandManager.handle(event);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        if (componentId.startsWith("queue:")) {

            String[] idParts = componentId.split(":");
            String action = idParts[1];
            int currentPage = Integer.parseInt(idParts[2]);

            QueueState state = musicService.getQueueState(event.getGuild().getIdLong());

            int itemsPerPage = 10;
            int totalPages = (int) Math.ceil((double) state.queue().size() / itemsPerPage);
            if (totalPages == 0) totalPages = 1;

            int newPage = currentPage;
            if (action.equals("next")) {
                newPage = Math.min(currentPage + 1, totalPages);
            } else if (action.equals("prev")) {
                newPage = Math.max(currentPage - 1, 1);
            }

            MessageEmbed newEmbed = buildQueueEmbed(state, newPage, itemsPerPage);

            Button newPrevButton = Button.primary("queue:prev:" + newPage, "Anterior")
                    .withDisabled(newPage == 1);
            Button newNextButton = Button.primary("queue:next:" + newPage, "Siguiente")
                    .withDisabled(newPage >= totalPages);

            event.editMessageEmbeds(newEmbed)
                    .setComponents(ActionRow.of(newPrevButton, newNextButton))
                    .queue();

        } else if (componentId.startsWith("pl-view:")) {

            String[] idParts = componentId.split(":", 4);

            String action = idParts[1];
            int currentPage = Integer.parseInt(idParts[2]);
            String playlistName = idParts[3];
            String guildId = event.getGuild().getId();

            try {
                PlayListEntity playlist = playListService.viewPlayList(playlistName, guildId);
                var items = playlist.getItems();

                int itemsPerPage = 10;
                int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
                if (totalPages == 0) totalPages = 1;

                int newPage = currentPage;
                if (action.equals("next")) {
                    newPage = Math.min(currentPage + 1, totalPages);
                } else if (action.equals("prev")) {
                    newPage = Math.max(currentPage - 1, 1);
                }

                MessageEmbed newEmbed = buildPlaylistViewEmbed(playlist, newPage);

                Button newPrevButton = Button.primary("pl-view:prev:" + newPage + ":" + playlistName, "Anterior")
                        .withDisabled(newPage == 1);
                Button newNextButton = Button.primary("pl-view:next:" + newPage + ":" + playlistName, "Siguiente")
                        .withDisabled(newPage >= totalPages);

                event.editMessageEmbeds(newEmbed)
                        .setComponents(ActionRow.of(newPrevButton, newNextButton))
                        .queue();

            } catch (Exception e) {
                event.replyEmbeds(buildErrorEmbed("Error", "No se pudo actualizar la lista: " + e.getMessage()))
                        .setEphemeral(true)
                        .queue();
            }

        } else if (componentId.startsWith("help:")) {
            String[] idParts = componentId.split(":");
            String action = idParts[1];
            int currentPage = Integer.parseInt(idParts[2]);

            int totalPages = 3;

            int newPage = currentPage;
            if (action.equals("next")) {
                newPage = Math.min(currentPage + 1, totalPages);
            } else if (action.equals("prev")) {
                newPage = Math.max(currentPage - 1, 1);
            }

            MessageEmbed newEmbed = buildHelpEmbed(
                    event.getJDA().getSelfUser(),
                    newPage,
                    totalPages
            );

            Button newPrevButton = Button.primary("help:prev:" + newPage, "Anterior")
                    .withDisabled(newPage == 1);
            Button newNextButton = Button.primary("help:next:" + newPage, "Siguiente")
                    .withDisabled(newPage >= totalPages);

            event.editMessageEmbeds(newEmbed)
                    .setComponents(ActionRow.of(newPrevButton, newNextButton))
                    .queue();
        } else if (componentId.startsWith("pl-delete")) {
            String[] idParts = componentId.split(":");
            String action = idParts[1];

            event.editComponents(
                    event.getMessage().getActionRows().get(0).withDisabled(true)
            ).queue();

            if (action.equals("cancel")) {
                MessageEmbed embed = buildSuccessEmbed("Cancelado", "La operación fue cancelada.");
                event.getHook().editOriginalEmbeds(embed).queue();
                return;
            }

            if (action.equals("confirm")) {
                String playlistName = idParts[2];
                String guildId = event.getGuild().getId();

                try {
                    playListService.deletePlayList(playlistName, guildId);

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
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("pl-load-select")) {
            String selectedPlaylistIdStr = event.getValues().get(0);
            Long playlistId = Long.parseLong(selectedPlaylistIdStr);

            if (event.getMember().getVoiceState().getChannel() == null) {
                event.reply("¡Debes estar en un canal de voz!").setEphemeral(true).queue();
                return;
            }

            try {
                PlayListEntity playlist = playListService.loadPlayListById(playlistId, event.getUser().getId());

                long guildId = event.getGuild().getIdLong();
                long voiceChannelId = event.getMember().getVoiceState().getChannel().getIdLong();
                long textChannelId = event.getChannel().getIdLong();

                for (PlayListItemEntity item : playlist.getItems()) {
                    musicService.loadAndPlay(guildId, voiceChannelId, textChannelId, item.getTrack_Identifier());
                }

                String msg = "Cargando **" + playlist.getItems().size() + "** canciones de la playlist **" + playlist.getName() + "**.";
                MessageEmbed embed = buildSuccessEmbed("Playlist Cargada", msg);

                event.editMessageEmbeds(embed)
                        .setContent("")
                        .setComponents()
                        .queue();

            } catch (Exception e) {
                MessageEmbed embed = buildErrorEmbed("Error", e.getMessage());

                event.editMessageEmbeds(embed)
                        .setContent("")
                        .setComponents()
                        .queue();
            }
        }
    }
}