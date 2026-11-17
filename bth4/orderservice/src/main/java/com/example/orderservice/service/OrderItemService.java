package com.example.orderservice.service;

import com.example.orderservice.dto.ProductDTO;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderService orderService;

    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    public OrderItem getOrderItemById(Integer id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy mặt hàng ID: " + id));
    }

    @Transactional
    public OrderItem createOrderItem(OrderItem itemReq, String authHeader) {
        if (itemReq.getOrderIdInput() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu orderId");
        }
        if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng sản phẩm phải lớn hơn 0!");
        }

        Order order = orderService.getOrderById(itemReq.getOrderIdInput());
        ProductDTO product = orderService.fetchProductFromService(itemReq.getProductId(), authHeader);

        if (product.getQuantity() == null || product.getQuantity() < itemReq.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm không đủ tồn kho!");
        }

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setQuantity(itemReq.getQuantity());
        item.setUnitPrice(product.getPrice());
        item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));

        OrderItem savedItem = orderItemRepository.save(item);
        order.getItems().add(savedItem);
        orderService.recalculateOrderTotal(order);
        orderService.updateProductStock(product, -itemReq.getQuantity(), authHeader);

        return savedItem;
    }

    @Transactional
    public OrderItem updateOrderItem(Integer id, OrderItem itemDetails, String authHeader) {
        if (itemDetails.getQuantity() == null || itemDetails.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng sản phẩm phải lớn hơn 0!");
        }

        OrderItem existingItem = getOrderItemById(id);
        int oldQty = existingItem.getQuantity();
        int newQty = itemDetails.getQuantity();
        Integer targetProductId = itemDetails.getProductId() != null
                ? itemDetails.getProductId()
                : existingItem.getProductId();

        // Trường hợp 1: Giữ nguyên sản phẩm cũ, chỉ cập nhật số lượng
        if (targetProductId.equals(existingItem.getProductId())) {
            int diff = newQty - oldQty;
            ProductDTO product = orderService.fetchProductFromService(existingItem.getProductId(), authHeader);

            if (diff > 0 && (product.getQuantity() == null || product.getQuantity() < diff)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không đủ tồn kho để tăng số lượng!");
            }

            existingItem.setQuantity(newQty);
            existingItem.setUnitPrice(product.getPrice());
            existingItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(newQty)));

            OrderItem updated = orderItemRepository.save(existingItem);
            orderService.recalculateOrderTotal(existingItem.getOrder());
            if (diff != 0) {
                orderService.updateProductStock(product, -diff, authHeader);
            }
            return updated;
        }

        // Trường hợp 2: Đổi sang sản phẩm mới
        ProductDTO newProduct = orderService.fetchProductFromService(targetProductId, authHeader);
        if (newProduct.getQuantity() == null || newProduct.getQuantity() < newQty) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sản phẩm mới '" + newProduct.getName() + "' không đủ tồn kho!");
        }

        // Hoàn lại tồn kho cho sản phẩm cũ và trừ tồn kho sản phẩm mới
        ProductDTO oldProduct = orderService.fetchProductFromService(existingItem.getProductId(), authHeader);
        orderService.updateProductStock(oldProduct, oldQty, authHeader);
        orderService.updateProductStock(newProduct, -newQty, authHeader);

        // Cập nhật thông tin sản phẩm mới vào chi tiết đơn hàng
        existingItem.setProductId(newProduct.getId());
        existingItem.setProductName(newProduct.getName());
        existingItem.setQuantity(newQty);
        existingItem.setUnitPrice(newProduct.getPrice());
        existingItem.setTotalPrice(newProduct.getPrice().multiply(BigDecimal.valueOf(newQty)));

        OrderItem updated = orderItemRepository.save(existingItem);
        orderService.recalculateOrderTotal(existingItem.getOrder());
        return updated;
    }

    @Transactional
    public void deleteOrderItem(Integer id, String authHeader) {
        OrderItem item = getOrderItemById(id);
        Order order = item.getOrder();

        // Hoàn lại tồn kho cho sản phẩm bị xóa khỏi đơn
        ProductDTO product = orderService.fetchProductFromService(item.getProductId(), authHeader);
        orderService.updateProductStock(product, item.getQuantity(), authHeader);

        order.getItems().remove(item);
        orderItemRepository.delete(item);
        orderService.recalculateOrderTotal(order);
    }
}