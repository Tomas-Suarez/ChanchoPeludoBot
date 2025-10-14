package com.chanchopeludo.ChanchoPeludoBot.util.helpers;

import java.net.URI;
import java.net.URISyntaxException;

public class ValidationHelper {

    private ValidationHelper() {}

    /**
     * Verifica si una cadena de texto es una URL.
     *
     * @param url La cadena a verificar.
     * @return true si es una URL válida, false en caso contrario.
     */
    public static boolean isUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
