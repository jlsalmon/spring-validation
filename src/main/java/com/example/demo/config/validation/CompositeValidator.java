package com.example.demo.config.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * Special implementation of a {@link Validator} that delegates to nested
 * validators to do the lifting. The validators will be called in order; if any
 * reports an error, the validation will be stopped.
 *
 * @author Justin Lewis Salmon
 */
@Slf4j
public class CompositeValidator implements Validator {

    private List<Validator> validators = new ArrayList<>();

    @Override
    public boolean supports(Class<?> clazz) {
        return validators.stream().anyMatch(v -> v.supports(clazz));
    }

    @Override
    public void validate(Object target, Errors errors) {
        for (Validator validator : validators) {
            if (!validator.supports(target.getClass())) {
                continue;
            }

            try {
                validator.validate(target, errors);
            } catch (Exception e) {
                throw new MessageConversionException("Exception thrown from validator " + validator.getClass(), e);
            }

            if (errors.getErrorCount() > 0) {
                return;
            }
        }
    }

    void addValidator(Validator validator) {
        this.validators.add(validator);
    }

    void addValidators(List<Validator> validators) {
        this.validators.addAll(validators);
    }
}
