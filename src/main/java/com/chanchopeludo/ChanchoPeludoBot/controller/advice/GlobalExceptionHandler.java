package com.chanchopeludo.ChanchoPeludoBot.controller.advice;

import com.chanchopeludo.ChanchoPeludoBot.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Map<Class<? extends Exception>, HttpStatus> exceptionStatusMap = new HashMap<>();

    public GlobalExceptionHandler() {
        exceptionStatusMap.put(ResourceNotFoundException.class, HttpStatus.NOT_FOUND);
        exceptionStatusMap.put(DuplicateResourceException.class, HttpStatus.CONFLICT);
        exceptionStatusMap.put(InvalidInputException.class, HttpStatus.BAD_REQUEST);
        exceptionStatusMap.put(ForbiddenException.class, HttpStatus.FORBIDDEN);
        exceptionStatusMap.put(ExternalServiceException.class, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ProblemDetail> handleCustomException(CustomException ex, HttpServletRequest request) {

        HttpStatus status = exceptionStatusMap.getOrDefault(ex.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);

        if (status.is5xxServerError()) {
            log.error(
                    "Internal server error [{}] - {} | Path: {}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    request.getRequestURI(),
                    ex
            );
        } else {
            log.warn(
                    "CLient error [{}] - {} | Path: {}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    request.getRequestURI()
            );
        }

            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());

            problemDetail.setTitle(status.getReasonPhrase());
            problemDetail.setType(URI.create("https://api.chanchopeludo.com/errors/" + ex.getClass().getSimpleName()));
            problemDetail.setInstance(URI.create(request.getRequestURI()));
            problemDetail.setProperty("timestamp", LocalDateTime.now());

            return ResponseEntity.status(status).body(problemDetail);

        }

    }
