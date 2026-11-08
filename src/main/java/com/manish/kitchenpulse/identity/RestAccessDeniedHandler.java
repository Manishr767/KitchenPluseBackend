package com.manish.kitchenpulse.identity;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manish.kitchenpulse.common.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** 403 for authenticated users who lack permission. */
@Component
class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    RestAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ErrorResponseWriter.write(response, mapper, HttpStatus.FORBIDDEN,
                "Access denied", "You do not have permission to perform this action.");
    }
}
