package br.documentation.exception;

public class FileNotAccessibleException extends RuntimeException{

    public FileNotAccessibleException(String message){
        super(message);
    }
}
