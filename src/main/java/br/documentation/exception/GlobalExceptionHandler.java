package br.documentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTime(LocalDateTime.now());
        errorResponse.setMessage("Validation failed");
        errorResponse.setDetails(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileNotAccessibleException.class)
    public ResponseEntity<Object> handlerFileNotFoundException(FileNotAccessibleException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorResponse(LocalDateTime.now(),
                        "File not found",
                        ex.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handlerAuthenticationException(AuthenticationException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorResponse(LocalDateTime.now(),
                        "Error making login",
                        ex.getMessage()),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UpdateTreeViewFile.class)
    public ResponseEntity<Object> handlerUpdateTreeViewFile(UpdateTreeViewFile ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorResponse(LocalDateTime.now(),
                        "Error saving TreeViewFile",
                        ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SaveFileException.class)
    public ResponseEntity<Object> handlerSaveFile(SaveFileException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ErrorResponse(LocalDateTime.now(),
                        "Error saving file",
                        ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }
}
