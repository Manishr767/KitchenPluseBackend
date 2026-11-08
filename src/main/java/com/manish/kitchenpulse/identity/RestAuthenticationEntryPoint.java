package com.manish.kitchenpulse.identity;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manish.kitchenpulse.common.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/** 401 for requests with no or an invalid token. */
@Component
class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    RestAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorResponseWriter.write(response, mapper, HttpStatus.UNAUTHORIZED,
                "Authentication required", "Missing, invalid or expired access token.");
    }
}
