package com.example.demo.config.validation;

import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public class ErrorUtils {

    public static List<Error> transformErrors(List<FieldError> fieldErrors) {
        List<Error> errors = new ArrayList<>();

        for (FieldError error : fieldErrors) {
            errors.add(new Error(
                error.getObjectName() + "." + error.getField() + "." + error.getCode(),
                error.getDefaultMessage(),
                error.getField(),
                error.getObjectName(),
                error.getRejectedValue()
            ));
        }

        return errors;
    }
}
