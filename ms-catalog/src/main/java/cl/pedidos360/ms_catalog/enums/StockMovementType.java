package cl.pedidos360.ms_catalog.enums;

/**
 * Tipos de movimientos que pueden modificar o registrar el stock
 * de un producto.
 *
 * <p>Cada movimiento permite mantener la trazabilidad de las operaciones
 * realizadas sobre el inventario.</p>
 */
public enum StockMovementType {

    /**
     * Carga inicial de stock al registrar un producto.
     */
    INITIAL_LOAD,

    /**
     * Reposición de unidades disponibles en inventario.
     */
    RESTOCK,

    /**
     * Ajuste manual realizado sobre el inventario.
     */
    ADJUSTMENT,

    /**
     * Descuento de stock producido por una venta.
     *
     * <p>En la arquitectura de Pedidos360, este movimiento será generado
     * como consecuencia de eventos provenientes de {@code ms-orders}
     * mediante SQS.</p>
     */
    SALE,

    /**
     * Devolución de unidades previamente vendidas.
     */
    RETURN
}
