package com.chanchopeludo.ChanchoPeludoBot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "user_server_stats")
@IdClass(UserServerStatsId.class)
public class UserServerStatsEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Id
    @ManyToOne
    @JoinColumn(name = "server_id")
    private ServerEntity server;

    private Long xp;

    private int level;
}
