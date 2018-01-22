package com.pragmaticcoders.checkout.checkoutcomponent.general.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collections;


@ControllerAdvice class RestResponseExceptionHandler extends ResponseEntityExceptionHandler {

    //TODO: this shouldnt be validation error
    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleProductNotFoundException(ResourceNotFoundException exc) {

        ApiValidationError apiValidationError = new ApiValidationError("NAME", exc.getMessage());
        apiValidationError.setRejectedValue(exc.getIdentity());

        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND);
        apiError.setMessage("Validation errors");
        apiError.setSubErrors(Collections.singletonList(apiValidationError));
        return buildResponseEntity(apiError);

    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleException(Exception exc) {

        ApiError apiError = new ApiError(HttpStatus.UNPROCESSABLE_ENTITY);
        apiError.setMessage(exc.getMessage());
        return buildResponseEntity(apiError);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
