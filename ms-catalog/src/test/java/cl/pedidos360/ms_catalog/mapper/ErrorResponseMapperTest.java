package cl.pedidos360.ms_catalog.mapper;

import cl.pedidos360.ms_catalog.dto.response.StandardErrorResponse;
import cl.pedidos360.ms_catalog.enums.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseMapperTest {

    private ErrorResponseMapper errorResponseMapper;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        errorResponseMapper = new ErrorResponseMapper();

        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/products");
        request.setMethod("GET");
    }

    @Test
    void toResponseShouldMapAllFields() {
        // Arrange
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        String message = "Producto no encontrado.";

        // Act
        StandardErrorResponse response = errorResponseMapper.toResponse(
                status,
                errorCode,
                message,
                request
        );

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(status.value());
        assertThat(response.code()).isEqualTo(errorCode.name());
        assertThat(response.error()).isEqualTo(status.getReasonPhrase());
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.path()).isEqualTo("/api/v1/products");
        assertThat(response.timestamp()).isPositive();
    }

    @Test
    void toResponseShouldUseProvidedHttpStatusAndErrorCode() {
        // Arrange
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorCode errorCode = ErrorCode.RESOURCE_ALREADY_EXISTS;

        // Act
        StandardErrorResponse response = errorResponseMapper.toResponse(
                status,
                errorCode,
                "El recurso ya existe.",
                request
        );

        // Assert
        assertThat(response.status()).isEqualTo(409);
        assertThat(response.code()).isEqualTo("RESOURCE_ALREADY_EXISTS");
        assertThat(response.error()).isEqualTo("Conflict");
        assertThat(response.message()).isEqualTo("El recurso ya existe.");
    }

    @Test
    void toValidationResponseShouldMapValidationErrors() {
        // Arrange
        Map<String, String> validationErrors = Map.of(
                "name", "El nombre es obligatorio.",
                "price", "El precio debe ser mayor que cero."
        );

        // Act
        StandardErrorResponse response =
                errorResponseMapper.toValidationResponse(
                        validationErrors,
                        request
                );

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.status())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.code())
                .isEqualTo(ErrorCode.VALIDATION_ERROR.name());
        assertThat(response.error())
                .isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(response.message())
                .isEqualTo("La solicitud contiene datos inválidos.");
        assertThat(response.path()).isEqualTo("/api/v1/products");
        assertThat(response.validationError())
                .containsExactlyInAnyOrderEntriesOf(validationErrors);
        assertThat(response.timestamp()).isPositive();
    }

    @Test
    void toValidationResponseShouldSupportEmptyValidationErrors() {
        // Arrange
        Map<String, String> validationErrors = Map.of();

        // Act
        StandardErrorResponse response =
                errorResponseMapper.toValidationResponse(
                        validationErrors,
                        request
                );

        // Assert
        assertThat(response.validationError()).isEmpty();
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.code())
                .isEqualTo(ErrorCode.VALIDATION_ERROR.name());
    }

    @Test
    void toInternalServerErrorShouldMapSafeMessageAndDeveloperMessage() {
        // Arrange
        Exception exception =
                new IllegalStateException("Database connection failed");

        // Act
        StandardErrorResponse response =
                errorResponseMapper.toInternalServerError(
                        exception,
                        request
                );

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.status())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.code())
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.name());
        assertThat(response.error())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        assertThat(response.message())
                .isEqualTo("Ha ocurrido un error interno en el servidor.");
        assertThat(response.developerMessage())
                .isEqualTo("Database connection failed");
        assertThat(response.path()).isEqualTo("/api/v1/products");
        assertThat(response.timestamp()).isPositive();
    }

    @Test
    void toInternalServerErrorShouldSupportExceptionWithoutMessage() {
        // Arrange
        Exception exception = new RuntimeException();

        // Act
        StandardErrorResponse response =
                errorResponseMapper.toInternalServerError(
                        exception,
                        request
                );

        // Assert
        assertThat(response.developerMessage()).isNull();
        assertThat(response.message())
                .isEqualTo("Ha ocurrido un error interno en el servidor.");
    }
}
