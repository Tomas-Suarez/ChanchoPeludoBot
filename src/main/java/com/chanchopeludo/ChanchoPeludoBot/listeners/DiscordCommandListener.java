package com.chanchopeludo.ChanchoPeludoBot.listeners;

import com.chanchopeludo.ChanchoPeludoBot.model.UserServerStatsEntity;
import com.chanchopeludo.ChanchoPeludoBot.service.MusicService;
import com.chanchopeludo.ChanchoPeludoBot.service.UserService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.AppConstants.DEFAULT_PREFIX;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.CommandConstants.*;

@Component
public class DiscordCommandListener extends ListenerAdapter {

    private final UserService userService;
    private final JDA jda;
    private final MusicService musicService;

    public DiscordCommandListener(UserService userService, JDA jda, MusicService musicService) {
        this.userService = userService;
        this.jda = jda;
        this.musicService = musicService;
    }

    @PostConstruct
    public void register() {
        jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String messageContent = event.getMessage().getContentRaw();
        String prefix = DEFAULT_PREFIX;

        if (!messageContent.startsWith(prefix)) {
            return;
        }

        String[] args = messageContent.substring(prefix.length()).split(" ", 2);
        String command = args[0].toLowerCase();

        if (command.equals("perfil")) {
            long userId = event.getAuthor().getIdLong();
            long serverId = event.getGuild().getIdLong();

            Optional<UserServerStatsEntity> optionalProfile = userService.getProfile(userId, serverId);

            if (optionalProfile.isPresent()) {
                UserServerStatsEntity profile = optionalProfile.get();
                String response = String.format(MSG_PROFILE_TEMPLATE,
                        event.getAuthor().getName(), profile.getLevel(), profile.getXp());
                event.getChannel().sendMessage(response).queue();
            } else {
                event.getChannel().sendMessage(MSG_PROFILE_NOT_FOUND).queue();
            }

        } else if (command.equals("play")) {

            if (args.length < 2) {
                event.getChannel().sendMessage(MSG_PLAY_USAGE).queue();
                return;
            }

            AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
            if (userChannel == null) {
                event.getChannel().sendMessage(MSG_PLAY_NO_VOICE).queue();
                return;
            }

            String url = args[1];
            musicService.loadAndPlayFromMessage(event, url);
        }
    }
}