package cl.pedidos360.ms_catalog.exception;

import cl.pedidos360.ms_catalog.enums.ErrorCode;
import lombok.Getter;

@Getter
public abstract class CatalogException extends RuntimeException {

    private final ErrorCode errorCode;

    public CatalogException(
            ErrorCode errorCode,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }
}
