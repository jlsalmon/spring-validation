package com.example.demo.config.rabbitmq;

import com.example.demo.config.validation.ErrorUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;

/**
 * @author Justin Lewis Salmon
 */
@Slf4j
public class ExceptionLoggingAdvice implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Exception e) {
            if (e.getCause() instanceof MethodArgumentNotValidException) {
                log.warn(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(
                    ErrorUtils.transformErrors(((MethodArgumentNotValidException) e.getCause()).getBindingResult().getFieldErrors())));
            }

            throw e;
        }
    }
}