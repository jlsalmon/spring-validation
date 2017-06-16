package com.example.demo;

import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public class CompositeValidator implements Validator {

    private List<Validator> validators = new ArrayList<>();

    @Override
    public boolean supports(Class<?> clazz) {
        for (Validator v : validators) {
            if (v.supports(clazz)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        for (Validator v : validators) {
            if (v.supports(target.getClass())) {
                try {
                    v.validate(target, errors);
                } catch (Exception e) {
                    throw new MessageConversionException("Exception thrown from validator " + v.getClass(), e);
                }

                if (errors.getErrorCount() > 0) {
                    return;
                }
            }
        }
    }

    public void addValidator(Validator validator) {
        this.validators.add(validator);
    }
}
