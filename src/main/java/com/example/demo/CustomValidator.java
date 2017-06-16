package com.example.demo;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Justin Lewis Salmon
 */
@Component
public class CustomValidator implements Validator {

    @Override
    public boolean supports(Class<?> type) {
        return type == DemoApplication.Pojo.class;
    }

    @Override
    public void validate(Object object, Errors errors) {
        DemoApplication.Pojo pojo = (DemoApplication.Pojo) object;

        if (pojo.getId().toString().length() == 4) {
            errors.rejectValue("id", "MatchesRegex", "id should match regex [a-zA-Z0-9]{8}");
        }
    }
}
