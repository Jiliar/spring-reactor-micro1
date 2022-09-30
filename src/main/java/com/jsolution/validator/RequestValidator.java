package com.jsolution.validator;

import com.jsolution.exception.MessageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RequestValidator {

    @Autowired
    private Validator validator;

    public <T> Mono<T> validate(T t){
        if(t == null){
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        }

        Set<ConstraintViolation<T>> constraints = validator.validate(t);

        String message = constraints.stream()
                .map(cons -> String.format("%s value '%s' %s", cons.getPropertyPath(),
                        cons.getInvalidValue(), cons.getMessage()))
                .collect(Collectors.joining());

        if(constraints == null || constraints.isEmpty()){
            return Mono.just(t);
        }

        return Mono.error(new MessageException(message));
    }
}
