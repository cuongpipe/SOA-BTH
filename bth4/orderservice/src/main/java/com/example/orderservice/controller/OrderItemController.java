package com.example.orderservice.controller;

import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/order_items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @GetMapping
    public ResponseEntity<List<OrderItem>> getAllOrderItems() {
        return ResponseEntity.ok(orderItemService.getAllOrderItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderItemById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(orderItemService.getOrderItemById(id));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrderItem(@RequestBody OrderItem orderItem,
                                             @RequestHeader("Authorization") String authHeader) {
        try {
            return new ResponseEntity<>(orderItemService.createOrderItem(orderItem, authHeader), HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderItem(@PathVariable Integer id,
                                             @RequestBody OrderItem orderItem,
                                             @RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(orderItemService.updateOrderItem(id, orderItem, authHeader));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrderItem(@PathVariable Integer id,
                                             @RequestHeader("Authorization") String authHeader) {
        try {
            orderItemService.deleteOrderItem(id, authHeader);
            return ResponseEntity.ok("Đã xóa mặt hàng trong đơn hàng thành công");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}