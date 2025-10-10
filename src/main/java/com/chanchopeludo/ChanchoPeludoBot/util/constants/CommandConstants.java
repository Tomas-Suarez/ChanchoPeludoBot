package com.chanchopeludo.ChanchoPeludoBot.util.constants;

public final class CommandConstants {

    private CommandConstants(){}

    // Mensajes de perfil
    public static final String MSG_PROFILE_NOT_FOUND = "Aún no tienes un perfil. ¡Habla en el servidor para ganar XP!";
    public static final String MSG_PROFILE_TEMPLATE = "¡Hola, %s!\nTu nivel es: %d\nTu XP es: %d";

    // Mensajes de play
    public static final String MSG_PLAY_USAGE = "Uso correcto: `c!play <URL de YouTube>`";
    public static final String MSG_PLAY_NO_VOICE = "Debes estar en un canal de voz para usar este comando.";

    // Mensajes para SlashCommand
    public static final String MSG_SLASH_NO_VOICE = "¡Debes estar en un canal de voz para usar este comando!";
}
