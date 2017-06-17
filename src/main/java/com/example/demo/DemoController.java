package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author Justin Lewis Salmon
 */
@RestController
public class DemoController {

    @RequestMapping("/test")
    public ResponseEntity test(@Valid @RequestBody Pojo pojo) {
        return ResponseEntity.ok(pojo);
    }

}
