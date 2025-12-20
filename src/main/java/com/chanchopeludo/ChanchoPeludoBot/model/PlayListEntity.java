package com.chanchopeludo.ChanchoPeludoBot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "playlist")
public class PlayListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private boolean is_public;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "server_id", nullable = false)
    private ServerEntity server;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private UserEntity creator;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "track_order")
    private List<PlayListItemEntity> items;

}
