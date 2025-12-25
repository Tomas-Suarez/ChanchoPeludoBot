package com.chanchopeludo.ChanchoPeludoBot.exceptions;

public class ResourceNotFoundException extends CustomException {
    public ResourceNotFoundException(String resourceName, String id) {
        super(String.format("%s no encontrado con el ID: %s", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s no encontrado con %s: '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
