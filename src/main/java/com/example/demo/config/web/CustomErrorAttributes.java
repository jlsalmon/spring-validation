package com.example.demo.config.web;

import com.example.demo.config.validation.ErrorUtils;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.RequestAttributes;

import java.util.List;
import java.util.Map;

/**
 * An extended version of {@link DefaultErrorAttributes} that customises the
 * error attributes, namely remapping {@link FieldError}s to {@link Error}s
 * to produce a cleaner output.
 *
 * @author Justin Lewis Salmon
 */
public class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean b) {
        Map<String, Object> attributes = super.getErrorAttributes(requestAttributes, b);
        attributes.remove("exception");

        if (attributes.containsKey("errors") && getError(requestAttributes) instanceof org.springframework.web.bind.MethodArgumentNotValidException) {
            attributes.put("errors", ErrorUtils
                .remapErrors((List<FieldError>) attributes.get("errors")));
        }

        return attributes;
    }

    @Override
    public Throwable getError(RequestAttributes requestAttributes) {
        return super.getError(requestAttributes);
    }
}