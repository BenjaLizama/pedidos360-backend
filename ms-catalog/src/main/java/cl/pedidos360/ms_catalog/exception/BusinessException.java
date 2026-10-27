package cl.pedidos360.ms_catalog.exception;

import cl.pedidos360.ms_catalog.enums.ErrorCode;

public class BusinessException extends CatalogException {
    public BusinessException(String message) {
        super(
                ErrorCode.INVALID_OPERATION,
                message
        );
    }
}
