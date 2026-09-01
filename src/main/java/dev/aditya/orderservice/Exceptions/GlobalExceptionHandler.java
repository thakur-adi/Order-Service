package dev.aditya.orderservice.Exceptions;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
/*
This is an Advisor Class. As the name suggests this handles all the exceptions defined in the project at  one place.
Helps us by eliminating unnecessary try{}catch{} code everywhere, Spring handles all that on its own
* */

@RestControllerAdvice
public class GlobalExceptionHandler {

    //Pre-Defined Exceptions

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNullPointerException(NullPointerException e){
        return new ResponseEntity<>("Null encountered. Please try again later!",HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(InternalError.class)
    public ResponseEntity<String> handleInternalError(InternalError e){
        return new ResponseEntity<>("Internal server issue encountered. Please try again later!",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e){
        return new ResponseEntity<>("Please enter a valid address and try again later!",HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleWrongResourceException(NoResourceFoundException e){
        return  new ResponseEntity<>("The requested address does not exist. Please verify the URL and HTTP method and try again!!",
                                    HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleWrongRequestMethodException(HttpRequestMethodNotSupportedException e){
        return  new ResponseEntity<>("The requested method is not supported. Please verify the URL and HTTP method and try again!!",
                HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e){
        return new ResponseEntity<>("Missing Message Body. Please provide a Http Body and try again!!",
                HttpStatus.BAD_REQUEST);
    }
    //For failed RestTemplate call i.e. this gets called when restTemplate call fails
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<String> handleHttpServerErrorException(HttpServerErrorException e){
        return new ResponseEntity<>("Couldn't generate payment link! Please try again later!",e.getStatusCode());
    }


    //Custom Exceptions
    @ExceptionHandler(CustomPaymentGenerationException.class)
    public ResponseEntity<String> handleCustomPaymentGenerationException(CustomPaymentGenerationException e){
        return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String> handleOrderNotFoundException(OrderNotFoundException e){
        return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
    }
}
