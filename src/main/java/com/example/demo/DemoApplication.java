package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@ControllerAdvice
@EnableRabbit
@SpringBootApplication
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DemoApplication implements RabbitListenerConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Data
    static class Pojo {
        @NotNull
        Integer id;
        @NotNull
        @Size(min = 2, max = 2)
        String name;
    }



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

    @Data
    @AllArgsConstructor
    public static class Error {
        private String code;
        private String message;
        private String field;
        private String objectName;
        private Object rejectedValue;
    }




    // Web
    @RequestMapping("/test")
    public ResponseEntity test(@Valid @RequestBody Pojo pojo) {
        return ResponseEntity.ok(pojo);
    }

    // Register composite validator with webmvc validation support
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(compositeValidator());
    }

    // Override the default error attributes to remove some of the
    // unnecessary stuff
    @Bean
    public ErrorAttributes errorAttributes() {
        return new MyErrorAttributes();
    }

    static class MyErrorAttributes extends DefaultErrorAttributes {

        @Override
        public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean b) {
            Map<String, Object> attributes = super.getErrorAttributes(requestAttributes, b);
            attributes.remove("exception");

            if (attributes.containsKey("errors") && getError(requestAttributes) instanceof org.springframework.web.bind.MethodArgumentNotValidException) {
                attributes.put("errors", CompositeValidator.ErrorUtils
                    .transformErrors((List<FieldError>) attributes.get("errors")));
            }

            return attributes;
        }

        @Override
        public Throwable getError(RequestAttributes requestAttributes) {
            return super.getError(requestAttributes);
        }
    }



    // AMQP
    @RabbitListener(queues = "test")
    public void listener(@Valid @Payload Pojo pojo) {
        log.info(pojo.toString());
    }

    // Enable JSON
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Register composite validator with AMQP infrastructure
    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(validatingHandlerMethodFactory());
    }

    @Bean
    public DefaultMessageHandlerMethodFactory validatingHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setValidator(compositeValidator());
        return factory;
    }

    // Register custom advice chain to log error messages
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        factory.setAdviceChain(new ExceptionLoggingAdvice());
        return factory;
    }

    class ExceptionLoggingAdvice implements MethodInterceptor {

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            try {
                return invocation.proceed();
            } catch (Exception e) {
                if (e.getCause() instanceof MethodArgumentNotValidException) {
                    log.warn(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(
                        CompositeValidator.ErrorUtils.transformErrors(((MethodArgumentNotValidException) e.getCause()).getBindingResult().getFieldErrors())));
                }

                throw e;
            }
        }
    }
}
