package com.example.demo.config.validation;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
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