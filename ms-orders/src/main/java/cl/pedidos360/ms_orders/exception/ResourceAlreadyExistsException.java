package cl.pedidos360.ms_orders.exception;

import cl.pedidos360.ms_orders.enums.ErrorCode;

public class ResourceAlreadyExistsException extends OrderException {
    public ResourceAlreadyExistsException(String message) {
        super(
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                message
        );
    }
}
