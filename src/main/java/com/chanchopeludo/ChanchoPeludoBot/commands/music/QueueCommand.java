package com.chanchopeludo.ChanchoPeludoBot.commands.music;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.dto.QueueState;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper;
import com.chanchopeludo.ChanchoPeludoBot.util.helpers.PaginationHelper;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.CommandConstants.ITEMS_PER_PAGE;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_NOT_IN_VOICE_CHANNEL;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.MSG_QUEUE_EMPTY;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildQueueEmbed;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.PaginationHelper.*;

@Component
public class QueueCommand implements Command {

    private final MusicService musicService;

    public QueueCommand(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash(getName(), "Muestra la cola de reproducción");
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        if (event.getMember().getVoiceState().getChannel() == null) {
            event.reply(MSG_NOT_IN_VOICE_CHANNEL).setEphemeral(true).queue();
            return;
        }

        QueueState state = musicService.getQueueState(event.getGuild().getIdLong());

        if (state.isEmpty()) {
            event.reply(MSG_QUEUE_EMPTY).setEphemeral(true).queue();
            return;
        }

        int totalPages = calculateTotalPages(state.queue().size(), ITEMS_PER_PAGE);

        MessageEmbed embed = buildQueueEmbed(state, 1, ITEMS_PER_PAGE);

        List<Button> buttons = createPaginationButtons("queue", 1, totalPages);

        event.replyEmbeds(embed).setActionRow(buttons).queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (event.getMember().getVoiceState().getChannel() == null) {
            event.getChannel().sendMessage(MSG_NOT_IN_VOICE_CHANNEL).queue();
            return;
        }

        QueueState state = musicService.getQueueState(event.getGuild().getIdLong());

        if (state.isEmpty()) {
            event.getChannel().sendMessage(MSG_QUEUE_EMPTY).queue();
            return;
        }

        int totalPages = calculateTotalPages(state.queue().size(), ITEMS_PER_PAGE);


        MessageEmbed embed = buildQueueEmbed(state, 1, ITEMS_PER_PAGE);

        List<Button> buttons = PaginationHelper.createPaginationButtons("queue", 1, totalPages);

        event.getChannel().sendMessageEmbeds(embed).setActionRow(buttons).queue();
    }

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("queue", "q");
    }

    @Override
    public boolean handlesButton(String componentId) {
        return componentId.startsWith("queue:");
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] idParts = event.getComponentId().split(":");
        String action = idParts[1];
        int currentPage = Integer.parseInt(idParts[2]);

        QueueState state = musicService.getQueueState(event.getGuild().getIdLong());

        int totalPages = calculateTotalPages(state.queue().size(), ITEMS_PER_PAGE);
        int newPage = calculateNewPage(action, currentPage, totalPages);

        MessageEmbed newEmbed = buildQueueEmbed(state, newPage, ITEMS_PER_PAGE);
        List<Button> buttons = createPaginationButtons("queue", newPage, totalPages);

        event.editMessageEmbeds(newEmbed)
                .setComponents(ActionRow.of(buttons))
                .queue();
    }
}