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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice(assignableTypes = PageController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebPageExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request) {
        String message = exception.getReason() == null
                ? exception.getStatusCode().toString()
                : exception.getReason();
        return error(exception.getStatusCode(), message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ModelAndView handleConstraintViolations(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ModelAndView handleInvalidArguments(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Invalid value for " + exception.getName(), request);
    }

    private ModelAndView error(HttpStatusCode status, String message, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("error");
        view.setStatus(status);
        view.addObject("status", status.value());
        view.addObject("message", message);
        view.addObject("returnPath", returnPath(request.getRequestURI()));
        return view;
    }

    private String returnPath(String requestPath) {
        if (requestPath.equals("/register")) {
            return "/register";
        }
        if (requestPath.equals("/profile") || requestPath.startsWith("/profile/")) {
            return "/profile";
        }
        if (requestPath.startsWith("/biddings/")) {
            String[] segments = requestPath.split("/");
            if (segments.length > 2 && segments[2].matches("\\d+")) {
                return "/biddings/" + segments[2];
            }
        }
        if (requestPath.startsWith("/auctions/") || requestPath.equals("/my-auctions")) {
            return "/my-auctions";
        }
        if (requestPath.startsWith("/payments/")) {
            String[] segments = requestPath.split("/");
            if (segments.length > 2 && segments[2].matches("\\d+")) {
                return "/payments/" + segments[2];
            }
        }
        if (requestPath.startsWith("/seller/")) {
            return "/seller/settings";
        }
        if (requestPath.startsWith("/admin/users")) {
            return "/admin/users";
        }
        if (requestPath.startsWith("/admin/auctions")) {
            return "/admin/auctions";
        }
        if (requestPath.startsWith("/admin/payments")) {
            return "/admin/payments";
        }
        if (requestPath.startsWith("/admin/bids")) {
            return "/admin/bids";
        }
        return "/";
    }
}
