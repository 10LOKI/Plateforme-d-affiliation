package com.affiliatelink.common;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.LinkedHashMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CampaignOwnershipException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleCampaignOwnership(CampaignOwnershipException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(InvalidOfferSearchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidOfferSearch(InvalidOfferSearchException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidParameter(MethodArgumentTypeMismatchException exception) {
        return Map.of("message", "Paramètre invalide : " + exception.getName());
    }

    @ExceptionHandler(OffreNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleOffreNotFound(OffreNotFoundException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(InactiveAdvertiserException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleInactiveAdvertiser(InactiveAdvertiserException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidJson(HttpMessageNotReadableException exception) {
        return Map.of("message", "Corps JSON absent, incorrect ou contenant une valeur non reconnue.");
    }

    @ExceptionHandler(InvalidProfileStatusException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleInvalidStatus(InvalidProfileStatusException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleProfileNotFound(ProfileNotFoundException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicateEmail(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation
                    && "23505".equals(violation.getSQLState())
                    && "offer_tags_name_key".equals(violation.getConstraintName())) {
                return Map.of("message", "Ce tag vient d’être créé par une autre requête. Réessayez.");
            }
            if (cause instanceof ConstraintViolationException violation
                    && "23505".equals(violation.getSQLState())
                    && "profile_profiles_email_key".equals(violation.getConstraintName())) {
                return Map.of("message", "Cet email est déjà utilisé.");
            }
            cause = cause.getCause();
        }
        throw exception;
    }
}
