package cl.eventpass.bff.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

/**
 * Preserva las respuestas HTTP de error recibidas desde los microservicios.
 */
@Slf4j
@RestControllerAdvice
public class DownstreamExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<String> handleDownstreamResponse(
            RestClientResponseException exception
    ) {
        log.warn(
                "Un servicio dependiente respondió con HTTP {}",
                exception.getStatusCode().value()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(
                exception.getResponseBodyAsString(),
                headers,
                exception.getStatusCode()
        );
    }
}
