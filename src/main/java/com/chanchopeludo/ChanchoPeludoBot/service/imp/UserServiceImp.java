package com.chanchopeludo.ChanchoPeludoBot.service.imp;

import com.chanchopeludo.ChanchoPeludoBot.exceptions.ResourceNotFoundException;
import com.chanchopeludo.ChanchoPeludoBot.model.ServerEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.UserEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.UserServerStatsEntity;
import com.chanchopeludo.ChanchoPeludoBot.model.UserServerStatsId;
import com.chanchopeludo.ChanchoPeludoBot.repository.ServerRepository;
import com.chanchopeludo.ChanchoPeludoBot.repository.UserRepository;
import com.chanchopeludo.ChanchoPeludoBot.repository.UserServerStatsRepository;
import com.chanchopeludo.ChanchoPeludoBot.service.UserService;
import com.chanchopeludo.ChanchoPeludoBot.util.helpers.LevelingHelper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.AppConstants.*;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.ResourceNames.USER_PROFILE;
import static com.chanchopeludo.ChanchoPeludoBot.util.constants.XpConstants.INITIAL_EXP;

@Service
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final UserServerStatsRepository statsRepository;

    public UserServiceImp(UserRepository userRepository, ServerRepository serverRepository, UserServerStatsRepository statsRepository) {
        this.userRepository = userRepository;
        this.serverRepository = serverRepository;
        this.statsRepository = statsRepository;
    }

    @Override
    public UserServerStatsEntity getProfile(String userId, String serverId) {
        UserServerStatsId statsId = new UserServerStatsId(userId, serverId);

        return statsRepository.findById(statsId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_PROFILE, userId));
    }

    @Override
    @Transactional
    public void addExp(String userId, String username, String serverId, String serverName, long xpToAdd) {
        UserEntity user = userRepository.findById(userId)
                .orElse(UserEntity.builder()
                        .idUser(userId)
                        .profile_image_url(null)
                        .build());

        user.setUsername(username);
        userRepository.save(user);

        ServerEntity server = serverRepository.findById(serverId)
                .orElse(ServerEntity.builder()
                        .idServer(serverId)
                        .prefix(DEFAULT_PREFIX)
                        .build());

        server.setGuild_name(serverName);
        serverRepository.save(server);

        UserServerStatsId statsId = new UserServerStatsId(userId, serverId);
        UserServerStatsEntity stats = statsRepository.findById(statsId).orElse(
                UserServerStatsEntity.builder()
                        .user(user)
                        .server(server)
                        .xp(INITIAL_EXP)
                        .level(1)
                        .build()
        );

        long currentXp = stats.getXp();
        stats.setXp(currentXp + xpToAdd);
        stats.setLevel(LevelingHelper.calculateLevel(stats.getXp()));

        statsRepository.save(stats);
    }

    @Override
    public int getLevel(String userId, String serverId) {
        UserServerStatsId statsId = new UserServerStatsId(userId, serverId);
        return statsRepository.findById(statsId)
                .map(UserServerStatsEntity::getLevel)
                .orElse(0);
    }
}