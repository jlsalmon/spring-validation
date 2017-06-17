package com.example.demo.config.validation;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Used to render error messages in validation failure responses.
 *
 * @author Justin Lewis Salmon
 */
@Data
@AllArgsConstructor
public class Error {
    private String code;
    private String message;
    private String field;
    private String objectName;
    private Object rejectedValue;
}