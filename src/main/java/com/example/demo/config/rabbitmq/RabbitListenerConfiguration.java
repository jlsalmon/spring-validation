package com.example.demo.config.rabbitmq;

import com.example.demo.config.validation.CompositeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.validation.Validator;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableRabbit
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RabbitListenerConfiguration implements RabbitListenerConfigurer {

    private final Validator compositeValidator;

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
        factory.setValidator(compositeValidator);
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
}
