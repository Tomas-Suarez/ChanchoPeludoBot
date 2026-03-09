package com.chanchopeludo.ChanchoPeludoBot.music;

import dev.arbjerg.lavalink.client.Link;
import lombok.Data;
import net.dv8tion.jda.api.JDA;

@Data
public class GuildMusicManager {
    private final Link link;
    private final TrackScheduler scheduler;
    private long lastTextChannelId;

    public GuildMusicManager(Link link, JDA jda) {
        this.link = link;
        this.scheduler = new TrackScheduler(this, link, jda);
    }
}