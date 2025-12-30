package com.chanchopeludo.ChanchoPeludoBot.commands.music;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.*;
import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildSuccessEmbed;

@Component
public class VolumeCommand implements Command {

    private final MusicService musicService;

    public VolumeCommand(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public CommandData getSlashCommandData() {
        OptionData volumeOption = new OptionData(OptionType.INTEGER, "nivel", "El nuevo volumen (0-120)", true)
                .setMinValue(0)
                .setMaxValue(120);

        return Commands.slash(getName(), "Ajusta el volumen de la música (0-120)")
                .addOptions(volumeOption);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {

        if (event.getMember().getVoiceState().getChannel() == null) {
            event.reply(MSG_NOT_IN_VOICE_CHANNEL).setEphemeral(true).queue();
            return;
        }

        int valueVolume = event.getOption("nivel").getAsInt();
        long guildId = event.getGuild().getIdLong();

        musicService.volume(event.getGuild().getIdLong(), valueVolume);

        event.replyEmbeds(buildSuccessEmbed("Volumen", String.format(MSG_VOLUME_MUSIC, valueVolume)))
                .queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        if (event.getMember().getVoiceState().getChannel() == null) {
            event.getChannel().sendMessage(MSG_NOT_IN_VOICE_CHANNEL).queue();
            return;
        }

        if (args.isEmpty()) {
            event.getChannel().sendMessage(MSG_INVALID_VALUE_VOLUME).queue();
            return;
        }

        try {
            int valueVolume = Integer.parseInt(args.get(0));
            long guildId = event.getGuild().getIdLong();

            musicService.volume(guildId, valueVolume);

            event.getChannel()
                    .sendMessageEmbeds(buildSuccessEmbed("Volumen", String.format(MSG_VOLUME_MUSIC, valueVolume)))
                    .queue();

        } catch (NumberFormatException e) {
            event.getChannel().sendMessage(MSG_INVALID_VALUE_VOLUME).queue();
        }
    }

    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("volume");
    }
}
