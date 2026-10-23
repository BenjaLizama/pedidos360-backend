package cl.pedidos360.ms_catalog.exception;

import cl.pedidos360.ms_catalog.dto.response.StandardErrorResponse;
import cl.pedidos360.ms_catalog.enums.ErrorCode;
import cl.pedidos360.ms_catalog.mapper.ErrorResponseMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ErrorResponseMapper errorResponseMapper;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest(
                "GET",
                "/api/v1/products"
        );
    }

    @Test
    void handleResourceNotFoundShouldReturnNotFound() {
        ResourceNotFoundException exception =
                mock(ResourceNotFoundException.class);

        when(exception.getErrorCode())
                .thenReturn(ErrorCode.VALIDATION_ERROR);
        when(exception.getMessage())
                .thenReturn("Producto no encontrado");

        var response = globalExceptionHandler
                .handleResourceNotFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(errorResponseMapper).toResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.VALIDATION_ERROR,
                "Producto no encontrado",
                request
        );
    }

    @Test
    void handleResourceAlreadyExistsShouldReturnConflict() {
        ResourceAlreadyExistsException exception =
                mock(ResourceAlreadyExistsException.class);

        when(exception.getErrorCode())
                .thenReturn(ErrorCode.RESOURCE_ALREADY_EXISTS);
        when(exception.getMessage())
                .thenReturn("El producto ya existe");

        var response = globalExceptionHandler
                .handleResourceAlreadyExists(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        verify(errorResponseMapper).toResponse(
                HttpStatus.CONFLICT,
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                "El producto ya existe",
                request
        );
    }

    @Test
    void handleInsufficientStockShouldReturnConflict() {
        InsufficientStockException exception =
                mock(InsufficientStockException.class);

        when(exception.getErrorCode())
                .thenReturn(ErrorCode.VALIDATION_ERROR);
        when(exception.getMessage())
                .thenReturn("Stock insuficiente");

        var response = globalExceptionHandler
                .handleInsufficientStock(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        verify(errorResponseMapper).toResponse(
                HttpStatus.CONFLICT,
                ErrorCode.VALIDATION_ERROR,
                "Stock insuficiente",
                request
        );
    }

    @Test
    void handleInvalidOperationShouldReturnConflict() {
        InvalidOperationException exception =
                mock(InvalidOperationException.class);

        when(exception.getErrorCode())
                .thenReturn(ErrorCode.VALIDATION_ERROR);
        when(exception.getMessage())
                .thenReturn("Operación no permitida");

        var response = globalExceptionHandler
                .handleInvalidOperation(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        verify(errorResponseMapper).toResponse(
                HttpStatus.CONFLICT,
                ErrorCode.VALIDATION_ERROR,
                "Operación no permitida",
                request
        );
    }

    @Test
    void handleBusinessExceptionShouldReturnConflict() {
        BusinessException exception = mock(BusinessException.class);

        when(exception.getErrorCode())
                .thenReturn(ErrorCode.VALIDATION_ERROR);
        when(exception.getMessage())
                .thenReturn("Error de negocio");

        var response = globalExceptionHandler
                .handleBusinessException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        verify(errorResponseMapper).toResponse(
                HttpStatus.CONFLICT,
                ErrorCode.VALIDATION_ERROR,
                "Error de negocio",
                request
        );
    }

    @Test
    void handleOptimisticLockingFailureShouldReturnConflict() {
        OptimisticLockingFailureException exception =
                new OptimisticLockingFailureException(
                        "Conflicto de concurrencia"
                );

        var response = globalExceptionHandler
                .handleOptimisticLockingFailure(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        verify(errorResponseMapper).toResponse(
                HttpStatus.CONFLICT,
                ErrorCode.OPTIMISTIC_LOCK_CONFLICT,
                "El recurso fue modificado por otro proceso. "
                        + "Vuelve a consultar el recurso e inténtalo nuevamente.",
                request
        );
    }

    @Test
    void handleValidationShouldReturnBadRequest() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = new BeanPropertyBindingResult(
                new Object(),
                "product"
        );

        bindingResult.addError(
                new FieldError(
                        "product",
                        "name",
                        "El nombre es obligatorio."
                )
        );

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        var response = globalExceptionHandler
                .handleValidation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(errorResponseMapper).toValidationResponse(
                Map.of("name", "El nombre es obligatorio."),
                request
        );
    }

    @Test
    void handleTypeMismatchShouldReturnBadRequest() {
        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);

        when(exception.getName())
                .thenReturn("productId");

        var response = globalExceptionHandler
                .handleTypeMismatch(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(errorResponseMapper).toResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "El parámetro 'productId' tiene un formato inválido.",
                request
        );
    }

    @Test
    void handleDuplicateKeyShouldReturnConflict() {
        DuplicateKeyException exception =
                new DuplicateKeyException("Duplicate key");

        var response = globalExceptionHandler
                .handleDuplicateKey(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        verify(errorResponseMapper).toResponse(
                HttpStatus.CONFLICT,
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                "El recurso que intenta crear o modificar ya existe.",
                request
        );
    }

    @Test
    void handleUnexpectedExceptionShouldReturnInternalServerError() {
        Exception exception =
                new RuntimeException("Database unavailable");

        StandardErrorResponse errorResponse =
                mock(StandardErrorResponse.class);

        when(errorResponseMapper.toInternalServerError(
                exception,
                request
        )).thenReturn(errorResponse);

        var response = globalExceptionHandler
                .handleUnexpectedException(exception, request);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertEquals(errorResponse, response.getBody());

        verify(errorResponseMapper).toInternalServerError(
                exception,
                request
        );
    }
}
