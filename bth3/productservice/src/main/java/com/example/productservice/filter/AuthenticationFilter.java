package com.example.productservice.filter;

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

        // Cấu hình URL giống hệt Postman ở Bài 2
        String authServiceUrl = "http://localhost:8080/auth"; 
        RestTemplate restTemplate = new RestTemplate();

        // Đưa Token vào Header thay vì nối vào URL
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader); // Truyền nguyên cụm "Bearer eyJhb..."
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Gửi request GET sang port 8080 kèm Header
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    authServiceUrl, 
                    HttpMethod.GET, 
                    entity, 
                    String.class
            );

            // Nếu Bài 2 trả về 200 OK (tức là pass xác thực)
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                filterChain.doFilter(request, response);
            }
        } catch (HttpClientErrorException e) {
            // Bắt lỗi 401 hoặc 403 từ Bài 2 trả về
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token khong hop le hoac da het han!");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Khong the ket noi den Auth Service (8080)");
        }
    }
}