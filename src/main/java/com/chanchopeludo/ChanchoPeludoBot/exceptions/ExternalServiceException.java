package com.chanchopeludo.ChanchoPeludoBot.exceptions;

public class ExternalServiceException extends CustomException {

    public ExternalServiceException(String serviceName, String detail) {
        super(String.format("Error de conexión con %s: %s", serviceName, detail));
    }
}
