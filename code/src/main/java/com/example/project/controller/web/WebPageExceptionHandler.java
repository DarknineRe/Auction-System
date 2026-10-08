package com.example.project.controller.web;

import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice(assignableTypes = PageController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebPageExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView handleResponseStatus(ResponseStatusException exception) {
        String message = exception.getReason() == null
                ? exception.getStatusCode().toString()
                : exception.getReason();
        return error(exception.getStatusCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ModelAndView handleConstraintViolations(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ModelAndView handleInvalidArguments(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return error(HttpStatus.BAD_REQUEST, "Invalid value for " + exception.getName());
    }

    private ModelAndView error(HttpStatusCode status, String message) {
        ModelAndView view = new ModelAndView("error");
        view.setStatus(status);
        view.addObject("status", status.value());
        view.addObject("message", message);
        return view;
    }
}
