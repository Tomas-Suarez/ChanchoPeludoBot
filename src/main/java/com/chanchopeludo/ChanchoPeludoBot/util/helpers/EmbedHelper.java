package com.chanchopeludo.ChanchoPeludoBot.util.helpers;

import com.chanchopeludo.ChanchoPeludoBot.dto.AudioTrackInfo;
import com.chanchopeludo.ChanchoPeludoBot.dto.QueueState;

import com.chanchopeludo.ChanchoPeludoBot.model.PlayListEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.PlayListItemEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.UserServerStatsEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.CommandConstants.MSG_HELP_FOOTER;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.CommandConstants.MSG_PROFILE_NOT_FOUND;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.MusicConstants.*;

public class EmbedHelper {

    public static MessageEmbed buildQueueEmbed(QueueState state, int page, int itemsPerPage) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(MSG_QUEUE_TITLE);

        AudioTrackInfo playingTrack = state.getNowPlaying().orElse(null);
        List<AudioTrackInfo> queueList = state.queue();

        if (playingTrack != null) {
            eb.addField(MSG_NOW_PLAYING, String.format("`%s`", playingTrack.title()), false);
        }

        if (queueList.isEmpty()) {
            eb.setDescription(MSG_QUEUE_EMPTY);
        } else {
            int start = (page - 1) * itemsPerPage;

            if (start >= queueList.size()) {
                eb.setDescription("\nNo hay más canciones en esta página.");
            } else {
                int end = Math.min(start + itemsPerPage, queueList.size());
                StringBuilder queueString = new StringBuilder();

                for (int i = start; i < end; i++) {
                    AudioTrackInfo track = queueList.get(i);
                    queueString.append(String.format("`%d.` %s\n", (i + 1), track.title()));
                }
                eb.addField(MSG_QUEUE_NEXT_UP, queueString.toString(), false);
            }
        }

        int totalPages = (int) Math.ceil((double) queueList.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;

        eb.setFooter(String.format(MSG_QUEUE_FOOTER, page, totalPages, queueList.size()));
        eb.setColor(0x1DB954);

        return eb.build();
    }

    private static String formatDuration(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    public static MessageEmbed buildNowPlayingEmbed(AudioTrackInfo currentTrack, long currentPosition) {
        String title = currentTrack.title();
        String duration = formatDuration(currentTrack.durationMs());
        String currentPosStr = formatDuration(currentPosition);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(MSG_NOW_PLAYING);
        eb.setDescription(String.format("**[%s](%s)**", title, currentTrack.url()));
        eb.setColor(0x1ED760);
        eb.addField("Progreso", String.format("%s / %s", currentPosStr, duration), true);

        return eb.build();
    }

    public static MessageEmbed buildHelpEmbed(SelfUser botUser, int page, int totalPages) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("📜 Lista de comandos");
        eb.setDescription("El prefijo para utilizar el bot es `c!`.");

        eb.setThumbnail(botUser.getEffectiveAvatarUrl());
        eb.setColor(0x3498DB);

        switch (page) {
            case 1:
                eb.setDescription("** Comandos de música **");
                eb.addField("🎶 Comandos de Música",
                        "`play (o p)`: Reproduce o añade una canción.\n" +
                                "`skip`: Salta a la siguiente canción.\n" +
                                "`stop`: Detiene la música y limpia la cola.\n" +
                                "`pause`: Pausa la reproducción.\n" +
                                "`resume`: Reanuda la reproducción.\n" +
                                "`queue`: Muestra la cola de canciones.\n" +
                                "`nowplaying (o np)`: Muestra la canción actual.\n" +
                                "`shuffle`: Mezcla la cola.\n" +
                                "`volume`: Ajusta el volumen (ej. `c!volume 50`).",
                        false);
                break;
        }

        eb.setFooter(String.format(MSG_HELP_FOOTER, page, totalPages), botUser.getEffectiveAvatarUrl());

        return eb.build();
    }

    /**
     * Crea un Embed de éxito genérico.
     *
     * @param title   El título del embed.
     * @param message El mensaje de descripción.
     * @return El MessageEmbed construido.
     */
    public static MessageEmbed buildSuccessEmbed(String title, String message) {
        return new EmbedBuilder()
                .setTitle("✅ " + title)
                .setDescription(message)
                .setColor(new Color(0x4CAF50))
                .build();
    }

    /**
     * Crea un Embed de error genérico.
     *
     * @param title   El título del error.
     * @param message El mensaje de descripción (ej. la excepción).
     * @return El MessageEmbed construido.
     */
    public static MessageEmbed buildErrorEmbed(String title, String message) {
        return new EmbedBuilder()
                .setTitle("⚠️ " + title)
                .setDescription(message)
                .setColor(Color.RED)
                .build();
    }

    public static MessageEmbed buildPerfilEmbed(User user, Optional<UserServerStatsEntity> optionalProfile) {

        if (optionalProfile.isEmpty()) {
            return buildErrorEmbed("Perfil no encontrado", MSG_PROFILE_NOT_FOUND);
        }

        UserServerStatsEntity profile = optionalProfile.get();
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Perfil de " + user.getName());

        eb.setThumbnail(user.getEffectiveAvatarUrl());

        eb.setColor(new Color(0x3498DB));

        eb.addField("Nivel", String.valueOf(profile.getLevel()), false);
        eb.addField("XP", String.valueOf(profile.getXp()), false);

        eb.setFooter("¡Sigue hablando para ganar más XP!", user.getJDA().getSelfUser().getEffectiveAvatarUrl());

        return eb.build();
    }

    public static MessageEmbed buildDisconnectEmbed() {
        return new EmbedBuilder()
                .setTitle("👋 ¡Desconectado por inactividad!")
                .setDescription("Me desconecté del canal de voz por estar 5 minutos inactivo.")
                .setColor(Color.YELLOW)
                .setTimestamp(Instant.now())
                .build();
    }

    public static MessageEmbed buildPlaylistViewEmbed(PlayListEntity playlist, int page) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("📂 Playlist: " + playlist.getName());
        eb.setColor(0x1DB954);

        String creatorMention = "<@" + playlist.getCreator().getIdUser() + ">";
        String serverName = playlist.getServer().getGuild_name();

        eb.addField("👤 Creador", creatorMention, false);
        eb.addField("🏠 Servidor origen", serverName, false);

        List<PlayListItemEntity> items = playlist.getItems();
        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;

        page = Math.max(1, Math.min(page, totalPages));

        if (items.isEmpty()) {
            eb.setDescription("☁️ La playlist está vacía.");
        } else {
            int start = (page - 1) * itemsPerPage;
            int end = Math.min(start + itemsPerPage, items.size());

            StringBuilder sb = new StringBuilder();
            for (int i = start; i < end; i++) {
                PlayListItemEntity item = items.get(i);
                sb.append(String.format("`%d.` %s\n", (i + 1), item.getTitle()));
            }
            eb.setDescription(sb.toString());
        }

        eb.setFooter(String.format("Página %d de %d (Total: %d canciones)", page, totalPages, items.size()));

        return eb.build();
    }
}
