package cl.pedidos360.ms_catalog.exception;

import cl.pedidos360.ms_catalog.enums.ErrorCode;

public class ResourceNotFoundException extends CatalogException {
    public ResourceNotFoundException(String message) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                message
        );
    }
}
