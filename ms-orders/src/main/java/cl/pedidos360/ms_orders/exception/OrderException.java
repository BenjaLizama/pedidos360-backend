package cl.pedidos360.ms_orders.exception;

import cl.pedidos360.ms_orders.enums.ErrorCode;
import lombok.Getter;

@Getter
public class OrderException extends RuntimeException {

    private final ErrorCode errorCode;

    public OrderException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
