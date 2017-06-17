package com.example.demo.config.validation;

import org.springframework.validation.FieldError;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Justin Lewis Salmon
 */
public class ErrorUtils {

    /**
     * Remap a list of {@link FieldError} into a list of {@link Error}.
     * <p>
     * This is used to customise and slim down the error representation.
     *
     * @param fieldErrors the list of {@link FieldError} to map
     * @return a list of {@link Error} remapped from the input
     */
    public static List<Error> remapErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream().map(error -> new Error(
                error.getObjectName() + "." + error.getField() + "." + error.getCode(),
                error.getDefaultMessage(),
                error.getField(),
                error.getObjectName(),
                error.getRejectedValue()
            )).collect(toList());
    }
}
