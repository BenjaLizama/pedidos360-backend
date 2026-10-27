package cl.pedidos360.ms_catalog.exception;

import cl.pedidos360.ms_catalog.enums.ErrorCode;

public class InsufficientStockException extends CatalogException {
    public InsufficientStockException(String message) {
        super(
                ErrorCode.INSUFFICIENT_STOCK,
                message
        );
    }
}
