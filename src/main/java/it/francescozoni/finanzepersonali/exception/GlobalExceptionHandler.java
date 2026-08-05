package it.francescozoni.finanzepersonali.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationError(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<String> errors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage
                        )
                        .toList();
        log.warn(
                "Errore validazione richiesta: {}",
                errors
        );
        return new ErrorResponse(
                LocalDateTime.now(),
                400,
                "VALIDATION_ERROR",
                errors,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MovimentoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse movimentoNonTrovato(
            MovimentoNotFoundException exception,
            HttpServletRequest request
    ) {

        log.warn(
                "Movimento non trovato: {}",
                exception.getMessage()
        );

        return new ErrorResponse(
                LocalDateTime.now(),
                404,
                "NOT_FOUND",
                List.of(exception.getMessage()),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(CategoriaNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse categoriaNonTrovata(
            CategoriaNotFoundException exception,
            HttpServletRequest request
    ) {

        log.warn(
                "Categoria non trovata: {}",
                exception.getMessage()
        );

        return new ErrorResponse(
                LocalDateTime.now(),
                404,
                "NOT_FOUND",
                List.of(exception.getMessage()),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse erroreGenerico(
            Exception exception,
            HttpServletRequest request
    ) {

        log.error(
                "Errore inatteso",
                exception
        );

        return new ErrorResponse(
                LocalDateTime.now(),
                500,
                "INTERNAL_SERVER_ERROR",
                List.of("Si è verificato un errore inatteso"),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse parametroErrato(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {

        log.warn(
                "Parametro errato: {}",
                exception.getValue()
        );

        return new ErrorResponse(
                LocalDateTime.now(),
                400,
                "INVALID_PARAMETER",
                List.of(
                        "Parametro non valido: " + exception.getName()
                ),
                request.getRequestURI()
        );
    }
}