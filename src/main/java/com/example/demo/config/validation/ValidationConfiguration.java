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

    // Generic validation stuff, register all validators for use with
    // web and amqp requests
    private final LocalValidatorFactoryBean validatorFactoryBean;
    private final ApplicationContext context;

    @Bean
    public Validator compositeValidator() {
        CompositeValidator composite = new CompositeValidator();
        composite.addValidator(validatorFactoryBean);

        context.getBeansOfType(Validator.class).values().forEach(validator -> {
            if (!validator.getClass().getName().startsWith("org.springframework")) {
                composite.addValidator(validator);
            }
        });

        return composite;
    }
}
