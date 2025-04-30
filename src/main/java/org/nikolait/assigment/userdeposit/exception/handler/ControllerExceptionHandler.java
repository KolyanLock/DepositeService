package org.nikolait.assigment.userdeposit.exception.handler;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.exception.DeletionException;
import org.nikolait.assigment.userdeposit.exception.TransferException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ErrorResponse handleBindExceptions(BindException ex) {
        return ErrorResponse.builder(ex, ProblemDetail.forStatus(HttpStatus.BAD_REQUEST))
                .type(URI.create(ex.getClass().getSimpleName()))
                .property("errors", extractFieldErrors(ex))
                .build();
    }

    @ExceptionHandler(ValidationException.class)
    public ErrorResponse handleValidationException(ValidationException ex) {
        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ErrorResponse handleEntityNotFound(EntityNotFoundException ex) {
        return ErrorResponse.builder(ex, HttpStatus.NOT_FOUND, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    @ExceptionHandler(EntityExistsException.class)
    public ErrorResponse handleEntityAlreadyExists(EntityExistsException ex) {
        return ErrorResponse.builder(ex, HttpStatus.CONFLICT, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    @ExceptionHandler(DeletionException.class)
    public ErrorResponse handleDeletionException(DeletionException ex) {
        return ErrorResponse.builder(ex, HttpStatus.CONFLICT, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    @ExceptionHandler(TransferException.class)
    public ErrorResponse handleTransferException(TransferException ex) {
        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ErrorResponse handleMissingHeader(MissingRequestHeaderException ex) {
        return ErrorResponse.builder(ex, HttpStatus.UNAUTHORIZED, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    private Map<String, String> extractFieldErrors(BindException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toUnmodifiableMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : ""
                ));
    }
}
