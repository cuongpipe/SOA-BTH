package com.example.orderservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Thieu Token xac thuc");
            return;
        }

        String authServiceUrl = "http://localhost:8080/auth";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    authServiceUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                filterChain.doFilter(request, response);
            }
        } catch (HttpClientErrorException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token khong hop le hoac da het han!");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Khong the ket noi den Auth Service (8080)");
        }
    }
}