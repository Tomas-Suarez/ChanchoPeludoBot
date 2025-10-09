package com.chanchopeludo.ChanchoPeludoBot;
import com.chanchopeludo.ChanchoPeludoBot.listeners.DiscordCommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class ChanchoPeludoBotApplication {

	@Value("${discord.token}")
	private String botToken;

	private final DiscordCommandListener discordCommandListener;

	public ChanchoPeludoBotApplication(DiscordCommandListener discordCommandListener) {
		this.discordCommandListener = discordCommandListener;
	}

	public static void main(String[] args) {
		SpringApplication.run(ChanchoPeludoBotApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void startBot() throws InterruptedException {
		JDA jda = JDABuilder.createDefault(botToken)
				.addEventListeners(discordCommandListener)
				.enableIntents(GatewayIntent.MESSAGE_CONTENT)
				.build();
		jda.awaitReady();
	}
}
