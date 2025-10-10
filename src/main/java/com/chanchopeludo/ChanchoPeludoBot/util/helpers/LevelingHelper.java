package com.chanchopeludo.ChanchoPeludoBot.util.helpers;

import static com.chanchopeludo.ChanchoPeludoBot.util.constants.XpConstants.XP_LEVEL_BASE;

public final class LevelingHelper {

    public static int calculateLevel(long xp){
        return (int) (Math.sqrt(xp / XP_LEVEL_BASE));
    }
}
