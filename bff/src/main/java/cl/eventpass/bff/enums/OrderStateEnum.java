package cl.eventpass.bff.enums;

/**
 * Enumeración que define los estados posibles de una orden de compra
 * en el microservicio ms-orders.
 *
 * <p>Representa las distintas etapas del ciclo de vida de una orden,
 * desde su creación hasta su entrega o cancelación.</p>
 *
 * <p>Los valores se utilizan para identificar el estado actual de la orden
 * y validar las transiciones permitidas por las reglas de negocio.</p>
 */
public enum OrderStateEnum {

    /**
     * La orden ha sido creada y aún no ha sido aceptada para su procesamiento.
     */
    CREADO,

    /**
     * La orden ha sido aceptada y puede continuar con su procesamiento.
     */
    ACEPTADO,

    /**
     * La orden se encuentra en preparación.
     */
    EN_PREPARACION,

    /**
     * La orden ha sido despachada y se encuentra en proceso de entrega.
     */
    DESPACHADO,

    /**
     * La orden ha sido entregada al cliente.
     */
    ENTREGADO,

    /**
     * La orden ha sido cancelada y no continuará con su procesamiento habitual.
     */
    CANCELADO
}
