package cl.pedidos360.ms_orders.exception;

import cl.pedidos360.ms_orders.enums.ErrorCode;

public class ResourceNotFoundException extends OrderException {
    public ResourceNotFoundException(String message) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                message
        );
    }
}
