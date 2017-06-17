package com.example.demo.config.web;

import com.example.demo.config.validation.CompositeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebConfiguration extends WebMvcConfigurerAdapter {

    private final CompositeValidator compositeValidator;

    // Register composite validator with webmvc validation support
    @Override
    public Validator getValidator() {
        return compositeValidator;
    }
    
    // Override the default error attributes to remove some of the
    // unnecessary stuff
    @Bean
    public ErrorAttributes errorAttributes() {
        return new CustomErrorAttributes();
    }
}
