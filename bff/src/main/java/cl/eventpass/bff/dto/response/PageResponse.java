package cl.eventpass.bff.dto.response;

import java.util.List;

/**
 * Representa una página de resultados recibida desde otro microservicio.
 *
 * @param content elementos incluidos en la página
 * @param page metadatos de paginación
 * @param <T> tipo de los elementos
 */
public record PageResponse<T>(
        List<T> content,
        PageMetadata page
) {

    /**
     * Metadatos de paginación de la respuesta.
     *
     * @param size tamaño solicitado de la página
     * @param number número de página actual, comenzando en cero
     * @param totalElements cantidad total de elementos
     * @param totalPages cantidad total de páginas
     */
    public record PageMetadata(
            int size,
            int number,
            long totalElements,
            int totalPages
    ) {
    }
}
