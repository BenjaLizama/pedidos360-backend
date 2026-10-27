package cl.pedidos360.ms_orders.exception;

import cl.pedidos360.ms_orders.enums.ErrorCode;

public class BusinessException extends OrderException {
    public BusinessException(String message) {
        super(
                ErrorCode.INVALID_OPERATION,
                message
        );
    }
}
