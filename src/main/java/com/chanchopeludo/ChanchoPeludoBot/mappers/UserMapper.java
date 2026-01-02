package com.chanchopeludo.ChanchoPeludoBot.mappers;

import com.chanchopeludo.ChanchoPeludoBot.dto.response.UserResponseDTO;
import com.chanchopeludo.ChanchoPeludoBot.model.UserServerStatsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", source = "user.idUser")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "serverId", source = "server.idServer")
    @Mapping(target = "serverName", source = "server.guild_name")
    UserResponseDTO toResponseDTO(UserServerStatsEntity entity);

}
