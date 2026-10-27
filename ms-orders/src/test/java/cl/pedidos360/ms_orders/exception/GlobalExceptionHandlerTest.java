package cl.pedidos360.ms_orders.exception;

import cl.pedidos360.ms_orders.dto.response.StandardErrorResponse;
import cl.pedidos360.ms_orders.enums.ErrorCode;
import cl.pedidos360.ms_orders.mapper.ErrorResponseMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private ErrorResponseMapper errorResponseMapper;
    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;
    private StandardErrorResponse errorResponse;

    @BeforeEach
    void setUp() {
        errorResponseMapper = mock(ErrorResponseMapper.class);
        handler = new GlobalExceptionHandler(errorResponseMapper);

        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/orders");
        request.setMethod("GET");

        errorResponse = mock(StandardErrorResponse.class);

        when(errorResponseMapper.toResponse(
                any(HttpStatus.class),
                any(ErrorCode.class),
                any(String.class),
                any(HttpServletRequest.class)
        )).thenReturn(errorResponse);
    }

    @Test
    void handleResourceNotFound_returns404() {
        ResourceNotFoundException exception =
                mock(ResourceNotFoundException.class);

        when(exception.getErrorCode()).thenReturn(ErrorCode.RESOURCE_NOT_FOUND);
        when(exception.getMessage()).thenReturn("Pedido no encontrado");

        ResponseEntity<StandardErrorResponse> response =
                handler.handleResourceNotFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertSame(errorResponse, response.getBody());

        verify(errorResponseMapper).toResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                "Pedido no encontrado",
                request
        );
    }

    @Test
    void handleBusinessException_returns409() {
        BusinessException exception = mock(BusinessException.class);

        when(exception.getErrorCode()).thenReturn(ErrorCode.INVALID_OPERATION);
        when(exception.getMessage()).thenReturn("Regla de negocio incumplida");

        ResponseEntity<StandardErrorResponse> response =
                handler.handleBusinessException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertSame(errorResponse, response.getBody());

        verify(errorResponseMapper).toResponse(
                HttpStatus.CONFLICT,
                ErrorCode.INVALID_OPERATION,
                "Regla de negocio incumplida",
                request
        );
    }

    @Test
    void handleInvalidOperation_returns409() {
        InvalidOperationException exception =
                mock(InvalidOperationException.class);

        when(exception.getErrorCode()).thenReturn(ErrorCode.INVALID_OPERATION);
        when(exception.getMessage()).thenReturn("Operación no permitida");

        ResponseEntity<StandardErrorResponse> response =
                handler.handleInvalidOperation(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertSame(errorResponse, response.getBody());

        verify(errorResponseMapper).toResponse(
                HttpStatus.CONFLICT,
                ErrorCode.INVALID_OPERATION,
                "Operación no permitida",
                request
        );
    }

    @Test
    void handleResourceAlreadyExists_returns409() {
        ResourceAlreadyExistsException exception =
                mock(ResourceAlreadyExistsException.class);

        when(exception.getErrorCode()).thenReturn(ErrorCode.RESOURCE_ALREADY_EXISTS);
        when(exception.getMessage()).thenReturn("El pedido ya existe");

        ResponseEntity<StandardErrorResponse> response =
                handler.handleResourceAlreadyExists(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertSame(errorResponse, response.getBody());

        verify(errorResponseMapper).toResponse(
                HttpStatus.CONFLICT,
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                "El pedido ya existe",
                request
        );
    }

    @Test
    void handleTypeMismatch_returns400() {
        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);

        when(exception.getName()).thenReturn("orderId");

        ResponseEntity<StandardErrorResponse> response =
                handler.handleTypeMismatch(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertSame(errorResponse, response.getBody());

        verify(errorResponseMapper).toResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "El parámetro 'orderId' tiene un formato inválido.",
                request
        );
    }

    @Test
    void handleUnreadableMessage_returns400() {
        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        ResponseEntity<StandardErrorResponse> response =
                handler.handleUnreadableMessage(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertSame(errorResponse, response.getBody());

        verify(errorResponseMapper).toResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "El cuerpo de la petición está vacío o contiene datos inválidos. Revisa el formato enviado.",
                request
        );
    }

    @Test
    void handleDataIntegrityViolation_returns409() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("Constraint violation");

        ResponseEntity<StandardErrorResponse> response =
                handler.handleDataIntegrityViolation(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertSame(errorResponse, response.getBody());

        verify(errorResponseMapper).toResponse(
                HttpStatus.CONFLICT,
                ErrorCode.INVALID_OPERATION,
                "La operación no puede completarse debido a una restricción de integridad de los datos.",
                request
        );
    }

    @Test
    void handleOptimisticLockingFailure_returns409() {
        OptimisticLockingFailureException exception =
                new OptimisticLockingFailureException("Concurrent update");

        ResponseEntity<StandardErrorResponse> response =
                handler.handleOptimisticLockingFailure(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertSame(errorResponse, response.getBody());

        verify(errorResponseMapper).toResponse(
                HttpStatus.CONFLICT,
                ErrorCode.OPTIMISTIC_LOCK_CONFLICT,
                "El recurso fue modificado por otro proceso. Vuelve a consultar el recurso e inténtalo nuevamente.",
                request
        );
    }

    @Test
    void handleTransactionException_returns500() {
        TransactionSystemException exception =
                new TransactionSystemException("Transaction failed");

        ResponseEntity<StandardErrorResponse> response =
                handler.handleTransactionException(exception, request);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );
        assertSame(errorResponse, response.getBody());

        verify(errorResponseMapper).toResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "No fue posible completar la operación debido a un error en la transacción.",
                request
        );
    }

    @Test
    void handleUnexpectedException_returns500() {
        Exception exception = new RuntimeException("Unexpected error");

        when(errorResponseMapper.toInternalServerError(exception, request))
                .thenReturn(errorResponse);

        ResponseEntity<StandardErrorResponse> response =
                handler.handleUnexpectedException(exception, request);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );
        assertSame(errorResponse, response.getBody());

        verify(errorResponseMapper)
                .toInternalServerError(exception, request);
    }
}
