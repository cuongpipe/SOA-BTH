package com.example.orderservice.service;

import com.example.orderservice.dto.ProductDTO;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    private final String PRODUCT_SERVICE_URL = "http://localhost:8081/products";

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng ID: " + id));
    }

    // Gọi sang Product Service (8081) kèm token để lấy thông tin & kiểm tra tồn kho
    public ProductDTO fetchProductFromService(Integer productId, String authHeader) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ProductDTO> response = restTemplate.exchange(
                    PRODUCT_SERVICE_URL + "/" + productId,
                    HttpMethod.GET,
                    entity,
                    ProductDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy sản phẩm ID " + productId + " bên Product Service");
        }
    }

    // Cập nhật lại số lượng tồn kho bên Product Service (8081)
    public void updateProductStock(ProductDTO product, int quantityDelta, String authHeader) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);

        product.setQuantity(product.getQuantity() + quantityDelta);
        HttpEntity<ProductDTO> entity = new HttpEntity<>(product, headers);

        restTemplate.exchange(
                PRODUCT_SERVICE_URL + "/" + product.getId(),
                HttpMethod.PUT,
                entity,
                ProductDTO.class
        );
    }

    private static final List<String> VALID_STATUSES = List.of("pending", "completed", "cancelled");

    @Transactional
    public Order createOrder(Order orderRequest, String authHeader) {
        String status = orderRequest.getStatus() != null ? orderRequest.getStatus().toLowerCase() : "pending";
        if (!VALID_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Trạng thái không hợp lệ. Chỉ chấp nhận: pending, completed, cancelled");
        }

        Order order = new Order();
        order.setCustomerName(orderRequest.getCustomerName());
        order.setCustomerEmail(orderRequest.getCustomerEmail());
        order.setStatus(status);

        BigDecimal totalAmount = BigDecimal.ZERO;

        if (orderRequest.getItems() != null && !orderRequest.getItems().isEmpty()) {
            for (OrderItem itemReq : orderRequest.getItems()) {
                if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng sản phẩm phải lớn hơn 0!");
                }

                ProductDTO product = fetchProductFromService(itemReq.getProductId(), authHeader);

                if (product.getQuantity() == null || product.getQuantity() < itemReq.getQuantity()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Sản phẩm '" + product.getName() + "' không đủ tồn kho (Còn: " + product.getQuantity() + ")");
                }

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(product.getPrice());
                item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));

                order.getItems().add(item);
                totalAmount = totalAmount.add(item.getTotalPrice());

                // Trừ tồn kho bên Product Service
                updateProductStock(product, -itemReq.getQuantity(), authHeader);
            }
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(Integer id, String newStatus, String authHeader) {
        if (newStatus == null || !VALID_STATUSES.contains(newStatus.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Trạng thái không hợp lệ. Chỉ chấp nhận: pending, completed, cancelled");
        }

        Order order = getOrderById(id);
        String oldStatus = order.getStatus();
        String normalizedNewStatus = newStatus.toLowerCase();

        // 1. Nếu hủy đơn (chuyển sang cancelled từ trạng thái khác) -> hoàn lại tồn kho
        if ("cancelled".equals(normalizedNewStatus) && !"cancelled".equalsIgnoreCase(oldStatus)) {
            for (OrderItem item : order.getItems()) {
                ProductDTO product = fetchProductFromService(item.getProductId(), authHeader);
                updateProductStock(product, item.getQuantity(), authHeader);
            }
        }
        // 2. Nếu khôi phục đơn từ cancelled sang pending/completed -> kiểm tra và trừ lại tồn kho
        else if (!"cancelled".equals(normalizedNewStatus) && "cancelled".equalsIgnoreCase(oldStatus)) {
            for (OrderItem item : order.getItems()) {
                ProductDTO product = fetchProductFromService(item.getProductId(), authHeader);
                if (product.getQuantity() == null || product.getQuantity() < item.getQuantity()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Sản phẩm '" + product.getName() + "' không đủ tồn kho để khôi phục đơn!");
                }
                updateProductStock(product, -item.getQuantity(), authHeader);
            }
        }

        order.setStatus(normalizedNewStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Integer id, String authHeader) {
        Order order = getOrderById(id);

        // Chỉ hoàn kho nếu đơn hàng chưa bị hủy trước đó
        if (!"cancelled".equalsIgnoreCase(order.getStatus())) {
            for (OrderItem item : order.getItems()) {
                ProductDTO product = fetchProductFromService(item.getProductId(), authHeader);
                updateProductStock(product, item.getQuantity(), authHeader);
            }
        }

        orderRepository.delete(order);
    }

    public void recalculateOrderTotal(Order order) {
        BigDecimal total = order.getItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
        orderRepository.save(order);
    }
}