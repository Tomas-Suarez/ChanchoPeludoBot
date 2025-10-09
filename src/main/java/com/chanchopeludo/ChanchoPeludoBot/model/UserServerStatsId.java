package com.chanchopeludo.ChanchoPeludoBot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserServerStatsId implements Serializable {
    private Long user;
    private Long server;
}
