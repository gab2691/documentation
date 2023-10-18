package br.documentation.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private LocalDateTime time;
    private String message;
    private String details;

    public ErrorResponse(){};
    public ErrorResponse(LocalDateTime time, String message, String details) {
        this.time = time;
        this.message = message;
        this.details = details;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
