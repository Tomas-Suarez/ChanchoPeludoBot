package com.chanchopeludo.ChanchoPeludoBot.commands.user;

import com.chanchopeludo.ChanchoPeludoBot.commands.Command;
import com.chanchopeludo.ChanchoPeludoBot.model.UserServerStatsEntity;
import com.chanchopeludo.ChanchoPeludoBot.service.UserService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.chanchopeludo.ChanchoPeludoBot.util.helpers.EmbedHelper.buildPerfilEmbed;

@Component
public class PerfilCommand implements Command {

    private UserService userService;

    public PerfilCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public CommandData getSlashCommandData() {
        return Commands.slash("perfil", "Muestra tu perfil en el servidor (nivel y XP en el servidor).");
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        Guild guild = event.getGuild();

        UserServerStatsEntity profile = userService.getProfile(user.getId(), guild.getId());

        MessageEmbed embed = buildPerfilEmbed(user, profile);

        event.replyEmbeds(embed).queue();
    }

    @Override
    public void executeText(MessageReceivedEvent event, List<String> args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        UserServerStatsEntity profile = userService.getProfile(user.getId(), guild.getId());

        MessageEmbed embed = buildPerfilEmbed(user, profile);

        event.getChannel().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "perfil";
    }

    @Override
    public List<String> getTextNames() {
        return Arrays.asList("perfil");
    }
}
