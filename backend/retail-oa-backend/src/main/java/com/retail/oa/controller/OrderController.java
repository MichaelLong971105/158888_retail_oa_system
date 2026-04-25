package com.retail.oa.controller;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-03-14 22:36
 **/

import com.retail.oa.dto.OrderRequest;
import com.retail.oa.dto.OrderResponse;
import com.retail.oa.service.OrderService;
import com.retail.oa.dto.OrderStatsResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import com.retail.oa.dto.UpdateOrderStatusRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Exposes purchase order APIs, including status updates and simple queries.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Creates a new purchase order.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    /**
     * Returns all orders or filters them by status when the query parameter is provided.
     */
    @GetMapping
    public List<OrderResponse> getAllOrders(@RequestParam(required = false) String status) {
        return orderService.getAllOrders(status);
    }

    /**
     * Returns one order by id.
     */
    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    /**
     * Updates the status of an existing order.
     */
    @PutMapping("/{id}/status")
    public OrderResponse updateOrderStatus(@PathVariable Long id,
                                           @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(id, request.getStatus());
    }

    /**
     * Returns summary statistics for the order module.
     */
    @GetMapping("/stats")
    public OrderStatsResponse getOrderStats() {
        return orderService.getOrderStats();
    }

    /**
     * Returns all orders belonging to one supplier.
     */
    @GetMapping("/supplier/{supplierId}")
    public List<OrderResponse> getOrdersBySupplierId(@PathVariable Long supplierId) {
        return orderService.getOrdersBySupplierId(supplierId);
    }

    /**
     * Returns orders created within the given inclusive date range.
     */
    @GetMapping("/date-range")
    public List<OrderResponse> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return orderService.getOrdersByDateRange(startDateTime, endDateTime);
    }
}
