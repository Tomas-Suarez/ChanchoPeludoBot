package com.chanchopeludo.ChanchoPeludoBot.util.helpers;

import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Arrays;
import java.util.List;

public class PaginationHelper {

    /**
     * Crea una lista con los botones de "Anterior" y "Siguiente" configurados correctamente.
     * Los botones se deshabilitan automáticamente si se está en la primera o última página.
     * <br>
     * El ID del botón tendrá el formato: {@code prefix:action:currentPage:extraData...}
     *
     * @param prefix      El prefijo identificador del comando (ej: "queue", "playlist-list").
     * @param currentPage El número de la página actual.
     * @param totalPages  El número total de páginas disponibles.
     * @param extraData   (Opcional) Datos extra a añadir al final del ID del botón (ej: scopes, filtros).
     * @return Una lista que contiene los dos botones (Anterior y Siguiente).
     */
    public static List<Button> createPaginationButtons(String prefix, int currentPage, int totalPages, String... extraData) {

        String suffix = extraData.length > 0 ? ":" + String.join(":", extraData) : "";

        String idPrev = prefix + ":prev:" + currentPage + suffix;
        String idNext = prefix + ":next:" + currentPage + suffix;

        Button prevButton = Button.primary(idPrev, "Anterior")
                .withDisabled(currentPage <= 1);

        Button nextButton = Button.primary(idNext, "Siguiente")
                .withDisabled(currentPage >= totalPages);

        return Arrays.asList(prevButton, nextButton);
    }

    /**
     * Calcula cuántas páginas totales se necesitan basándose en la cantidad de elementos.
     *
     * @param totalItems   La cantidad total de elementos (canciones, playlists, etc.).
     * @param itemsPerPage La cantidad de elementos que se muestran por cada página.
     * @return El número total de páginas. Si la lista está vacía, devuelve 1.
     * @throws IllegalArgumentException Si itemsPerPage es menor o igual a 0.
     */
    public static int calculateTotalPages(long totalItems, int itemsPerPage) {
        if (itemsPerPage <= 0) throw new IllegalArgumentException("itemsPerPage debe ser mayor a 0");

        int pages = (int) Math.ceil((double) totalItems / itemsPerPage);

        return pages == 0 ? 1 : pages;
    }

    /**
     * Calcula cuál será la nueva página basándose en la acción del botón ("next" o "prev").
     *
     * @param action      La acción recibida del botón ("next" o "prev").
     * @param currentPage Página actual.
     * @param totalPages  El límite máximo de páginas.
     * @return El nuevo número de página.
     */
    public static int calculateNewPage(String action, int currentPage, int totalPages) {
        if ("next".equalsIgnoreCase(action)) {
            return Math.min(currentPage + 1, totalPages);
        } else if ("prev".equalsIgnoreCase(action)) {
            return Math.max(currentPage - 1, 1);
        }
        return currentPage;
    }

    /**
     * Genera el texto estándar para el pie de página (Footer) de los Embeds.
     *
     * @param currentPage Página actual.
     * @param totalPages  Total de páginas.
     * @param totalItems  Cantidad total de elementos en la lista.
     * @return Un String formateado: "Página X de Y • Total: Z"
     */
    public static String getPageFooter(int currentPage, int totalPages, long totalItems) {
        return "Página " + currentPage + " de " + totalPages + " • Total: " + totalItems;
    }
}
