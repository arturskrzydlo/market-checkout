package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collections;

/**
 * Created by artur.skrzydlo on 2017-05-14.
 */
@ControllerAdvice
public class RestResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    protected ResponseEntity<Object> handleProductNotFoundException(ProductNotFoundException exc) {

        ApiValidationError apiValidationError = new ApiValidationError("NAME", exc.getMessage());
        apiValidationError.setRejectedValue(exc.getProductName());

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
