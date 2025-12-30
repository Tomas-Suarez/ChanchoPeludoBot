package com.chanchopeludo.ChanchoPeludoBot.commands.music;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.commands.music.handlers.InputHandler;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.InvalidInputException;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.*;

@Component
public class PlayCommand implements Command {

    private final List<InputHandler> handlers;

    public PlayCommand(List<InputHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public CommandData getSlashCommandData() {
        OptionData option = new OptionData(OptionType.STRING, "url", "La URL o nombre de la canción", true);
        return Commands.slash(getName(), "Reproduce una canción o búsqueda")
                .addOptions(option);
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {

        final AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
        if(userChannel == null){
            event.reply(MSG_NOT_IN_VOICE_CHANNEL).setEphemeral(true).queue();
            return;
        }

        long guildId = event.getGuild().getIdLong();
        long voiceChannelId = userChannel.getIdLong();
        long textChannelId = event.getChannel().getIdLong();
        String input = event.getOption("url").getAsString();

        event.deferReply().setContent(MSG_SEARCH_MUSIC).queue();

        handlePlayLogic(guildId, voiceChannelId, textChannelId, input)
                .thenAccept(message -> event.getChannel().sendMessage(message).queue())
                .exceptionally(ex -> {
                    return null;
                });
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {

        if(args.isEmpty()){
            event.getChannel().sendMessage(MSG_PLAY_USAGE).queue();
            return;
        }

        final AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
        if(userChannel == null){
            event.getChannel().sendMessage(MSG_NOT_IN_VOICE_CHANNEL).queue();
            return;
        }

        long guildId = event.getGuild().getIdLong();
        long voiceChannelId = userChannel.getIdLong();
        long textChannelId = event.getChannel().getIdLong();
        String input = String.join(" ", args);

        event.getChannel().sendMessage(MSG_SEARCH_MUSIC).queue();

        handlePlayLogic(guildId, voiceChannelId, textChannelId, input)
                .thenAccept(message -> event.getChannel().sendMessage(message).queue())
                .exceptionally(ex -> {
                    return null;
                });
    }

    private CompletableFuture<String> handlePlayLogic(long guildId, long voiceChannelId, long textChannelId, String input) {
        return handlers.stream()
                .filter(handler -> handler.canHandle(input))
                .findFirst()
                .map(handler -> handler.handle(guildId, voiceChannelId, textChannelId, input))
                .orElseThrow(() -> new InvalidInputException("No se pudo procesar la entrada: " + input));
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("play", "p");
    }
}