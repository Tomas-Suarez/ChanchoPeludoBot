package com.chanchopeludo.ChanchoPeludoBot.listeners;

import com.chanchopeludo.ChanchoPeludoBot.model.UserServerStatsEntity;
import com.chanchopeludo.ChanchoPeludoBot.service.UserService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.chanchopeludo.ChanchoPeludoBot.util.AppConstants.DEFAULT_PREFIX;

@Component
public class DiscordCommandListener extends ListenerAdapter {

    private final UserService userService;

    public DiscordCommandListener(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String messageContent = event.getMessage().getContentRaw();

        if(messageContent.equalsIgnoreCase(DEFAULT_PREFIX + "perfil")){
            long userId = event.getAuthor().getIdLong();
            long serverId = event.getGuild().getIdLong();

            Optional<UserServerStatsEntity> optionalProfile = userService.getProfile(userId, serverId);

            if (optionalProfile.isPresent()) {
                UserServerStatsEntity profile = optionalProfile.get();
                String response = "¡Hola, " + event.getAuthor().getName() + "!\n" +
                        "Tu nivel es: " + profile.getLevel() + "\n" +
                        "Tu XP es: " + profile.getXp();
                event.getChannel().sendMessage(response).queue();
            } else {
                event.getChannel().sendMessage("Aún no tienes un perfil. ¡Habla en el servidor para ganar XP!").queue();
            }
        }
    }
}