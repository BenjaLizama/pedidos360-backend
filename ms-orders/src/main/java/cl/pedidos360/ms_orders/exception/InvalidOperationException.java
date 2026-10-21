package cl.pedidos360.ms_orders.exception;

import cl.pedidos360.ms_orders.enums.ErrorCode;

public class InvalidOperationException extends OrderException {
    public InvalidOperationException(String message) {
        super(
                ErrorCode.INVALID_OPERATION,
                message
        );
    }
}
