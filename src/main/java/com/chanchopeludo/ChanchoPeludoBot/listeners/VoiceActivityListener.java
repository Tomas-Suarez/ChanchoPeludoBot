package com.chanchopeludo.ChanchoPeludoBot.listeners;

import dev.arbjerg.lavalink.client.LavalinkClient;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
public class VoiceActivityListener extends ListenerAdapter {
    private final LavalinkClient lavalinkClient;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<Long, ScheduledFuture<?>> disconnectTasks = new ConcurrentHashMap<>();

    public VoiceActivityListener(LavalinkClient lavalinkClient) {
        this.lavalinkClient = lavalinkClient;
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        long guildId = event.getGuild().getIdLong();
        Member self = event.getGuild().getSelfMember();

        if (self.getVoiceState() == null || self.getVoiceState().getChannel() == null) {
            return;
        }

        int humanCount = 0;
        for (Member m : self.getVoiceState().getChannel().getMembers()) {
            if (!m.getUser().isBot()) {
                humanCount++;
            }
        }

        if (humanCount == 0) {
            if (!disconnectTasks.containsKey(guildId)) {
                ScheduledFuture<?> task = scheduler.schedule(() -> {
                    disconnect(guildId, event.getGuild().getAudioManager());
                }, 5, TimeUnit.MINUTES);

                disconnectTasks.put(guildId, task);
                log.info("Bot solo en Guild {}. Desconexión programada en 5 min.", guildId);
            }
        } else {
            ScheduledFuture<?> task = disconnectTasks.remove(guildId);
            if (task != null) {
                task.cancel(false);
                log.info("Alguien entró en Guild {}. Desconexión cancelada.", guildId);
            }
        }
    }

    private void disconnect(long guildId, net.dv8tion.jda.api.managers.AudioManager audioManager) {
        lavalinkClient.getOrCreateLink(guildId)
                .createOrUpdatePlayer()
                .setTrack(null)
                .subscribe();

        audioManager.closeAudioConnection();
        disconnectTasks.remove(guildId);
        log.info("El bot se desconectó por inactividad en la Guild: {}", guildId);
    }
}