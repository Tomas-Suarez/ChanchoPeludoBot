package com.chanchopeludo.ChanchoPeludoBot.exceptions;

public class DuplicateResourceException extends CustomException {

    public DuplicateResourceException(String resourceName, String value) {
        super(String.format("%s '%s' ya existe.", resourceName, value));
    }
}
