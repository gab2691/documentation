package br.documentation.exception;

public class AuthenticationException extends RuntimeException{

    public AuthenticationException(String message, Exception e){
        super(message);
    }
}
