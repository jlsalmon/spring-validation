package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.validation.Valid;

/**
 * @author Justin Lewis Salmon
 */
@Slf4j
@Component
public class DemoListener {

    @RabbitListener(queues = "test")
    public void listener(@Valid @Payload Pojo pojo) {
        log.info(pojo.toString());
    }

}
