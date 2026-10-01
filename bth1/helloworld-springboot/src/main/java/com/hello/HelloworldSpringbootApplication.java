package com.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController // 1. Báo cho Spring Boot biết class này sẽ trả chữ về cho web
public class HelloworldSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloworldSpringbootApplication.class, args);
    }

    // 2. Khi vào localhost:8080/ thì bắn chữ này lên màn hình
    @GetMapping("/")
    public String index() {
        return "Hello World!";
    }
}