package cl.pedidos360.ms_catalog.exception;

import cl.pedidos360.ms_catalog.enums.ErrorCode;

public class InvalidOperationException extends CatalogException {
    public InvalidOperationException(String message) {
        super(
                ErrorCode.INVALID_OPERATION,
                message
        );
    }
}
