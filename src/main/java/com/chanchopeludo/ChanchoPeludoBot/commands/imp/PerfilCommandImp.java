package com.chanchopeludo.ChanchoPeludoBot.commands.imp;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.model.UserServerStatsEntity;
import com.chanchopeludo.ChanchoPeludoBot.service.UserService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.CommandConstants.MSG_PROFILE_NOT_FOUND;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.CommandConstants.MSG_PROFILE_TEMPLATE;

@Component
public class PerfilCommandImp implements Command {
    private final UserService userService;

    public PerfilCommandImp(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
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
    }

    @Override
    public String getName() {
        return "perfil";
    }
}
