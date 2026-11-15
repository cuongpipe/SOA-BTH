package com.example.orderservice.controller;

import com.example.orderservice.entity.Order;
import com.example.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(id));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order,
                                         @RequestHeader("Authorization") String authHeader) {
        try {
            return new ResponseEntity<>(orderService.createOrder(order, authHeader), HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer id,
                                               @RequestBody Map<String, String> body,
                                               @RequestHeader("Authorization") String authHeader) {
        try {
            String status = body.get("status");
            return ResponseEntity.ok(orderService.updateOrderStatus(id, status, authHeader));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Integer id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok("Đã xóa đơn hàng thành công");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}