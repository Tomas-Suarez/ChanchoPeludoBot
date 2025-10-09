package com.chanchopeludo.ChanchoPeludoBot.util;

public final class LevelingHelper {

    public static int calculateLevel(long xp){
        return (int) (Math.sqrt(xp / AppConstants.XP_LEVEL_BASE));
    }
}
