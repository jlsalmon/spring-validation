package com.example.demo.config.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ValidationConfiguration {

    private final LocalValidatorFactoryBean validatorFactoryBean;
    private final ApplicationContext context;

    @Bean
    public CompositeValidator compositeValidator() {
        CompositeValidator composite = new CompositeValidator();

        // Register the built-in bean validator (jsr303)
        composite.addValidator(validatorFactoryBean);

        // Register custom validators
        context.getBeansOfType(Validator.class).values().forEach(validator -> {
            if (!validator.getClass().getName().startsWith("org.springframework")) {
                composite.addValidator(validator);
            }
        });

        return composite;
    }
}
