package cl.pedidos360.ms_catalog.exception;

import cl.pedidos360.ms_catalog.enums.ErrorCode;

public class ResourceAlreadyExistsException extends CatalogException {
    public ResourceAlreadyExistsException(String message) {
        super(
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                message
        );
    }
}
