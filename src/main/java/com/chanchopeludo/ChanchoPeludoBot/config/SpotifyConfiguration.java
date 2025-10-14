package com.chanchopeludo.ChanchoPeludoBot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.SpotifyConstants.TOKEN_FAILURE;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.SpotifyConstants.TOKEN_SUCCESS;

@Configuration
public class SpotifyConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyConfiguration.class);

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Bean
    public SpotifyApi spotifyApi() {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();

        try {
            ClientCredentialsRequest credentialsRequest = spotifyApi.clientCredentials().build();
            String accessToken = credentialsRequest.execute().getAccessToken();
            spotifyApi.setAccessToken(accessToken);
            logger.info(TOKEN_SUCCESS);
        } catch (Exception e) {
            logger.error(TOKEN_FAILURE, e);
        }
        return spotifyApi;
    }
}