package com.retail.oa.service;

import com.retail.oa.dto.OrderItemRequest;
import com.retail.oa.dto.OrderItemResponse;
import com.retail.oa.dto.OrderRequest;
import com.retail.oa.dto.OrderResponse;
import com.retail.oa.entity.*;
import com.retail.oa.exception.InsufficientStockException;
import com.retail.oa.exception.InvalidOperationException;
import com.retail.oa.exception.ResourceNotFoundException;
import com.retail.oa.exception.SupplierNotFoundException;
import com.retail.oa.repository.InventoryLogRepository;
import com.retail.oa.repository.OrderRepository;
import com.retail.oa.repository.ProductRepository;
import com.retail.oa.dto.OrderStatsResponse;
import com.retail.oa.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles purchase order creation, status changes, stock-in logic, and order queries.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final SupplierRepository supplierRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        InventoryLogRepository inventoryLogRepository,
                        SupplierRepository supplierRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryLogRepository = inventoryLogRepository;
        this.supplierRepository = supplierRepository;
    }

    /**
     * Creates a new purchase order in PENDING status without changing product stock.
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        Supplier supplier = supplierRepository.findById(orderRequest.getSupplierId())
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + orderRequest.getSupplierId()));

        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()
                    ));

            Integer quantity = itemRequest.getQuantity();

            if (quantity <= 0) {
                throw new InsufficientStockException("Quantity must be greater than 0");
            }

            BigDecimal itemPrice = product.getPrice();
            BigDecimal subtotal = itemPrice.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(itemPrice);

            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setSupplier(supplier);

        Order savedOrder = orderRepository.save(order);
        return convertToOrderResponse(savedOrder);
    }

    /**
     * Returns all orders or filters them by status.
     */
    @Transactional
    public List<OrderResponse> getAllOrders(String status) {
        List<Order> orders;

        if (status == null || status.isBlank()) {
            orders = orderRepository.findAll();
        } else {
            OrderStatus parsedStatus = parseStatus(status);
            orders = parsedStatus == OrderStatus.RECEIVED
                    ? orderRepository.findByStatusIn(receivedStatuses())
                    : orderRepository.findByStatus(parsedStatus);
        }

        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {
            responses.add(convertToOrderResponse(order));
        }

        return responses;
    }

    /**
     * Returns a single order by id.
     */
    @Transactional
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        return convertToOrderResponse(order);
    }

    /**
     * Converts an order entity into the DTO expected by the API layer.
     */
    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setSupplierId(order.getSupplier().getId());
        response.setSupplierName(order.getSupplier().getName());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setStatus(toApiStatus(order.getStatus()).name());

        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setId(item.getId());
            itemResponse.setProductId(item.getProduct().getId());
            itemResponse.setProductName(item.getProduct().getName());
            itemResponse.setSku(item.getProduct().getSku());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());

            BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            itemResponse.setSubtotal(subtotal);

            itemResponses.add(itemResponse);
        }

        response.setItems(itemResponses);
        return response;
    }

    /**
     * Generates a readable order number for new purchase orders.
     */
    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().getYear() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Validates whether the given status belongs to the allowed purchase order states.
     */
    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new InvalidOperationException("Order status cannot be blank");
        }

        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidOperationException("Invalid order status: " + status);
        }
    }

    /**
     * Treats older COMPLETED purchase orders as RECEIVED in the public API.
     */
    private OrderStatus toApiStatus(OrderStatus status) {
        if (status == OrderStatus.COMPLETED) {
            return OrderStatus.RECEIVED;
        }

        return status;
    }

    private List<OrderStatus> receivedStatuses() {
        // COMPLETED is a legacy database value from before the purchase order workflow used RECEIVED.
        return List.of(OrderStatus.RECEIVED, OrderStatus.COMPLETED);
    }

    /**
     * Validates one-way order status transitions.
     */
    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.PENDING) {
            return newStatus == OrderStatus.RECEIVED || newStatus == OrderStatus.CANCELLED;
        }

        return false;
    }

    /**
     * Performs stock-in and writes inventory logs when an order is received.
     */
    private void stockInForReceivedOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            Integer quantity = item.getQuantity();

            product.setStock(product.getStock() + quantity);
            linkSupplierIfMissing(product, order.getSupplier());
            productRepository.save(product);

            InventoryLog inventoryLog = new InventoryLog();
            inventoryLog.setProduct(product);
            inventoryLog.setChangeType("IN");
            inventoryLog.setQuantity(quantity);
            inventoryLog.setRemark("Stock in for received order " + order.getOrderNumber());
            inventoryLogRepository.save(inventoryLog);
        }
    }

    private void linkSupplierIfMissing(Product product, Supplier supplier) {
        boolean alreadyLinked = product.getSuppliers().stream()
                .anyMatch(existingSupplier -> supplier.getId().equals(existingSupplier.getId()));

        if (!alreadyLinked) {
            product.getSuppliers().add(supplier);
        }
    }

    /**
     * Updates the order status and applies stock changes only for PENDING to RECEIVED.
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        OrderStatus newStatus = parseStatus(status);
        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == null) {
            currentStatus = OrderStatus.PENDING;
        }

        if (currentStatus == newStatus) {
            return convertToOrderResponse(order);
        }

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidOperationException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus
            );
        }

        if (newStatus == OrderStatus.RECEIVED) {
            stockInForReceivedOrder(order);
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        return convertToOrderResponse(updatedOrder);
    }

    /**
     * Calculates simple summary statistics for all orders.
     */
    public OrderStatsResponse getOrderStats() {
        OrderStatsResponse stats = new OrderStatsResponse();

        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long receivedOrders = orderRepository.countByStatusIn(receivedStatuses());
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal receivedAmount = BigDecimal.ZERO;

        List<Order> allOrders = orderRepository.findAll();
        for (Order order : allOrders) {
            if (order.getTotalAmount() != null) {
                totalAmount = totalAmount.add(order.getTotalAmount());
            }
        }

        List<Order> receivedOrderList = orderRepository.findByStatusIn(receivedStatuses());
        for (Order order : receivedOrderList) {
            if (order.getTotalAmount() != null) {
                receivedAmount = receivedAmount.add(order.getTotalAmount());
            }
        }

        stats.setTotalOrders(totalOrders);
        stats.setPendingOrders(pendingOrders);
        stats.setReceivedOrders(receivedOrders);
        stats.setCancelledOrders(cancelledOrders);
        stats.setTotalAmount(totalAmount);
        stats.setReceivedAmount(receivedAmount);

        return stats;
    }

    /**
     * Returns all orders belonging to a supplier after validating the supplier exists.
     */
    @Transactional
    public List<OrderResponse> getOrdersBySupplierId(Long supplierId) {
        supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + supplierId));

        List<Order> orders = orderRepository.findBySupplierId(supplierId);
        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {
            responses.add(convertToOrderResponse(order));
        }

        return responses;
    }

    /**
     * Returns all orders created between the provided timestamps.
     */
    @Transactional
    public List<OrderResponse> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new InsufficientStockException("Start date and end date cannot be null");
        }

        if (start.isAfter(end)) {
            throw new InsufficientStockException("Start date cannot be after end date");
        }

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);
        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {
            responses.add(convertToOrderResponse(order));
        }

        return responses;
    }

}
