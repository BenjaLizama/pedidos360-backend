package cl.pedidos360.ms_catalog.enums;

/**
 * Códigos funcionales utilizados para clasificar los errores
 * controlados que puede producir el microservicio de catálogo.
 *
 * <p>Estos códigos permiten diferenciar el tipo de error de negocio
 * independientemente del código HTTP utilizado en la respuesta.</p>
 */
public enum ErrorCode {

    /**
     * El recurso solicitado no existe.
     */
    RESOURCE_NOT_FOUND,

    /**
     * El recurso que se intenta crear o registrar ya existe.
     */
    RESOURCE_ALREADY_EXISTS,

    /**
     * La operación no puede realizarse porque el stock disponible
     * es insuficiente.
     */
    INSUFFICIENT_STOCK,

    /**
     * La operación solicitada no es válida según las reglas
     * del dominio.
     */
    INVALID_OPERATION,

    /**
     * La solicitud contiene datos que no cumplen las validaciones
     * definidas por la API.
     */
    VALIDATION_ERROR,

    /**
     * Ocurrió un error inesperado no contemplado por las excepciones
     * controladas de la aplicación.
     */
    INTERNAL_SERVER_ERROR,

    /**
     * Indica que el recurso fue modificado por otro proceso
     * después de haber sido leído, provocando un conflicto
     * de concurrencia optimista.
     */
    OPTIMISTIC_LOCK_CONFLICT
}
