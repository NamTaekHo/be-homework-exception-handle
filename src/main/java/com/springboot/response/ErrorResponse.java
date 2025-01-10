package com.springboot.response;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.validation.ConstraintViolation;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class ErrorResponse {
//    private List<NullPointerError> nullPointerErrors;
    private Integer status;
    private String message;
    private List<FieldError> fieldErrors;
    private List<ConstraintViolationError> violationErrors;


    private ErrorResponse(final Integer status,
                          final String message,
                          final List<FieldError> fieldErrors,
                          final List<ConstraintViolationError> violationErrors) {
        this.status = status;
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.violationErrors = violationErrors;
    }

    public static ErrorResponse of(BusinessLogicException businessLogicException){
        return new ErrorResponse(businessLogicException.getExceptionCode().getStatus(), businessLogicException.getMessage(), null, null);
    }

    public static ErrorResponse of(BindingResult bindingResult) {
        return new ErrorResponse(null, null, FieldError.of(bindingResult), null);
    }

    public static ErrorResponse of(Set<ConstraintViolation<?>> violations) {
        return new ErrorResponse(null, null, null, ConstraintViolationError.of(violations));
    }

    public static ErrorResponse of(HttpRequestMethodNotSupportedException exception){
        return new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(), null, null);
    }

    public static ErrorResponse of(Exception e){
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), null, null);
    }
    @Getter
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String reason;

        private FieldError(String field, Object rejectedValue, String reason) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        public static List<FieldError> of(BindingResult bindingResult) {
            final List<org.springframework.validation.FieldError> fieldErrors =
                                                        bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() == null ?
                                            "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    public static class ConstraintViolationError {
        private String propertyPath;
        private Object rejectedValue;
        private String reason;

        private ConstraintViolationError(String propertyPath, Object rejectedValue,
                                   String reason) {
            this.propertyPath = propertyPath;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        public static List<ConstraintViolationError> of(
                Set<ConstraintViolation<?>> constraintViolations) {
            return constraintViolations.stream()
                    .map(constraintViolation -> new ConstraintViolationError(
                            constraintViolation.getPropertyPath().toString(),
                            constraintViolation.getInvalidValue().toString(),
                            constraintViolation.getMessage()
                    )).collect(Collectors.toList());
        }
    }
}