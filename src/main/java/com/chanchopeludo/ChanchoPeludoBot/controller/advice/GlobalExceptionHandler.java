package com.chanchopeludo.ChanchoPeludoBot.controller.advice;

import com.chanchopeludo.ChanchoPeludoBot.exceptions.CustomException;
import com.chanchopeludo.ChanchoPeludoBot.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Map<Class<? extends Exception>, HttpStatus> exceptionStatusMap = new HashMap<>();

    public GlobalExceptionHandler(){
        exceptionStatusMap.put(ResourceNotFoundException.class, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ProblemDetail> handleCustomException(CustomException ex, HttpServletRequest request) {

        HttpStatus status = exceptionStatusMap.getOrDefault(ex.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());

        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setType(URI.create("https://api.chanchopeludo.com/errors/" + ex.getClass().getSimpleName()));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(status).body(problemDetail);

    }

}
