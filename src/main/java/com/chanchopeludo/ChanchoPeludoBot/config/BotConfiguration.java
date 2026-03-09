package com.chanchopeludo.ChanchoPeludoBot.config;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;

import java.net.URI;
import java.util.Base64;

@Configuration
public class BotConfiguration {

    @Value("${discord.token}")
    private String token;

    @Value("${lavalink.host}")
    private String lavalinkHost;

    @Value("${lavalink.port}")
    private int lavalinkPort;

    @Value("${lavalink.password}")
    private String lavalinkPassword;

    @Bean
    public LavalinkClient lavalinkClient() {
        long botId = extractBotIdFromToken(token);

        LavalinkClient client = new LavalinkClient(botId);

        NodeOptions nodeOptions = new NodeOptions.Builder()
                .setName("Nodo-Principal")
                .setServerUri(URI.create("ws://" + lavalinkHost + ":" + lavalinkPort))
                .setPassword(lavalinkPassword)
                .build();

        client.addNode(nodeOptions);
        return client;
    }

    @Bean
    public JDA jda(LavalinkClient lavalinkClient) throws InterruptedException {
        JDA jda = JDABuilder.createDefault(token)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalinkClient))
                .enableIntents(
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS
                )
                .build()
                .awaitReady();

        return jda;
    }

    private long extractBotIdFromToken(String botToken) {
        try {
            String base64Id = botToken.split("\\.")[0];
            byte[] decodedBytes = Base64.getDecoder().decode(base64Id);
            String decodedString = new String(decodedBytes);
            return Long.parseLong(decodedString);
        } catch (Exception e) {
            System.err.println("No se pudo extraer el ID del bot desde el token. Revisa tu .env");
            return 0L;
        }
    }
}