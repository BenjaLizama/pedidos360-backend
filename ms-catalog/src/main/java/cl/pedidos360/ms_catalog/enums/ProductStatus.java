package cl.pedidos360.ms_catalog.enums;

/**
 * Estados posibles del ciclo de vida de un producto del catálogo.
 *
 * <p>El estado permite controlar la disponibilidad lógica del producto
 * sin necesidad de eliminar físicamente su documento de MongoDB.</p>
 */
public enum ProductStatus {

    /**
     * Producto disponible y habilitado para las operaciones normales
     * del catálogo.
     */
    ACTIVE,

    /**
     * Producto deshabilitado temporalmente.
     *
     * <p>El producto permanece almacenado para conservar su historial,
     * pero no se encuentra disponible para las operaciones que requieran
     * un producto activo.</p>
     */
    INACTIVE,

    /**
     * Producto retirado definitivamente del catálogo.
     *
     * <p>El registro se conserva para mantener la trazabilidad histórica
     * de las operaciones realizadas sobre el producto.</p>
     */
    DISCONTINUED
}
