package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@ControllerAdvice
@EnableRabbit
@SpringBootApplication
public class DemoApplication implements RabbitListenerConfigurer {

    @Autowired
    private LocalValidatorFactoryBean validatorFactoryBean;

    @Autowired
    private ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @RequestMapping("/test")
    public ResponseEntity test(@Valid @RequestBody Pojo pojo) {
        return ResponseEntity.ok(pojo);
    }

    @RabbitListener(queues = "test")
    public void listener(@Valid @Payload Pojo pojo) {
        log.info(pojo.toString());
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(compositeValidator());
    }

    @Bean
    public ErrorAttributes errorAttributes() {
        return new MyErrorAttributes();
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(validatingHandlerMethodFactory());
    }

    @Bean
    private DefaultMessageHandlerMethodFactory validatingHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory =
            new DefaultMessageHandlerMethodFactory();
        factory.setValidator(compositeValidator());
        return factory;
    }

    @Bean
    private SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory listenerContainerFactory =
            new SimpleRabbitListenerContainerFactory();
        listenerContainerFactory.setConnectionFactory(connectionFactory);
        listenerContainerFactory.setErrorHandler(
            new ConditionalRejectingErrorHandler(
                new InvalidPayloadRejectingFatalExceptionStrategy()));
        listenerContainerFactory.setMessageConverter(jackson2JsonMessageConverter());
        return listenerContainerFactory;
    }

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

    /**
     * Extension of Spring-AMQP's
     * {@link ConditionalRejectingErrorHandler.DefaultExceptionStrategy}
     * which also considers a root cause of {@link MethodArgumentNotValidException}
     * (thrown when payload does not validate) as fatal.
     */
    @Slf4j
    static class InvalidPayloadRejectingFatalExceptionStrategy implements FatalExceptionStrategy {

        @Override
        public boolean isFatal(Throwable t) {
            if (t instanceof ListenerExecutionFailedException &&
                (t.getCause() instanceof MessageConversionException || t.getCause() instanceof MethodArgumentNotValidException)) {
                log.warn("Fatal message conversion error; message rejected; it will be dropped: {}",
                    ((ListenerExecutionFailedException) t).getFailedMessage());
                return true;
            }
            return false;
        }
    }

    @Data
    static class Pojo {
        @NotNull
        Integer id;
        @NotNull
        @Size(min = 2, max = 2)
        String name;
    }

    static class MyErrorAttributes extends DefaultErrorAttributes {

        @Override
        public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean b) {
            Map<String, Object> attributes = super.getErrorAttributes(requestAttributes, b);
            attributes.remove("exception");

            if (attributes.containsKey("errors") && getError(requestAttributes) instanceof org.springframework.web.bind.MethodArgumentNotValidException) {
                Collection<Error> errors = new ArrayList<>();

                for (FieldError error : (List<FieldError>) attributes.get("errors")) {
                    errors.add(new Error(
                        error.getObjectName() + "." + error.getField() + "." + error.getCode(),
                        error.getDefaultMessage(),
                        error.getField(),
                        error.getObjectName(),
                        error.getRejectedValue()
                    ));
                }

                attributes.put("errors", errors);
            }

            return attributes;
        }

        @Override
        public Throwable getError(RequestAttributes requestAttributes) {
            return super.getError(requestAttributes);
        }
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
}
