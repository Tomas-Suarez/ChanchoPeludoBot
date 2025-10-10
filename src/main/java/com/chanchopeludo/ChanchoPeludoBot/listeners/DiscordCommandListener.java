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

import static com.chanchopeludo.ChanchoPeludoBot.util.AppConstants.DEFAULT_PREFIX;

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
                String response = "¡Hola, " + event.getAuthor().getName() + "!\n" +
                        "Tu nivel es: " + profile.getLevel() + "\n" +
                        "Tu XP es: " + profile.getXp();
                event.getChannel().sendMessage(response).queue();
            } else {
                event.getChannel().sendMessage("Aún no tienes un perfil. ¡Habla en el servidor para ganar XP!").queue();
            }

        } else if (command.equals("play")) {

            if (args.length < 2) {
                event.getChannel().sendMessage("Uso correcto: `c!play <URL de YouTube>`").queue();
                return;
            }

            AudioChannel userChannel = event.getMember().getVoiceState().getChannel();
            if (userChannel == null) {
                event.getChannel().sendMessage("Debes estar en un canal de voz para usar este comando.").queue();
                return;
            }

            String url = args[1];
            musicService.loadAndPlayFromMessage(event, url);
        }
    }
}