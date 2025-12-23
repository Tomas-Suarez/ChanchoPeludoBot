package com.chanchopeludo.ChanchoPeludoBot.exceptions;

public class ResourceNotFoundException extends CustomException {
    public ResourceNotFoundException(String resourceName, String id) {
        super(String.format("%s no encontrado con ID: %s", resourceName, id));
    }
}
