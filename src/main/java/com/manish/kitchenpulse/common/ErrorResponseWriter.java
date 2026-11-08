package com.manish.kitchenpulse.common;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;

/**
 * Writes an RFC 7807-style JSON error from places Spring MVC's exception
 * handling does not reach (the security filter chain).
 */
public final class ErrorResponseWriter {

    private ErrorResponseWriter() {
    }

    public static void write(HttpServletResponse response, ObjectMapper mapper,
                             HttpStatus status, String title, String detail) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", title);
        body.put("status", status.value());
        body.put("detail", detail);

        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/problem+json");
        mapper.writeValue(response.getWriter(), body);
    }
}
