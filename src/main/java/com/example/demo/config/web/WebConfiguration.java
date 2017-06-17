package com.example.demo.config.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebConfiguration {

    private final Validator compositeValidator;

    // Register composite validator with webmvc validation support
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(compositeValidator);
    }

    // Override the default error attributes to remove some of the
    // unnecessary stuff
    @Bean
    public ErrorAttributes errorAttributes() {
        return new CustomErrorAttributes();
    }
}
