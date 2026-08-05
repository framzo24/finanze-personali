package it.francescozoni.finanzepersonali.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse (
        LocalDateTime timestamp,

        int status,

        String error,

        List<String> messages,

        String path
){
}
