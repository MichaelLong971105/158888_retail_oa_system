package com.retail.oa.controller;

import com.retail.oa.dto.sale.MockSalesRequest;
import com.retail.oa.dto.sale.SaleIngestRequest;
import com.retail.oa.dto.sale.SaleRecordResponse;
import com.retail.oa.dto.sale.SalesDashboardResponse;
import com.retail.oa.dto.sale.TopSellingProductResponse;
import com.retail.oa.entity.SaleSource;
import com.retail.oa.service.SalesService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Exposes sales management APIs and a POS-ready ingest interface.
 */
@RestController
@RequestMapping("/sales")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    /**
     * Creates one manual sales record inside the OA system.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleRecordResponse createSale(@Valid @RequestBody SaleIngestRequest request) {
        return salesService.createManualSale(request);
    }

    /**
     * Ingests one future POS sale payload using the public integration shape.
     */
    @PostMapping("/ingest")
    @ResponseStatus(HttpStatus.CREATED)
    public SaleRecordResponse ingestPosSale(@Valid @RequestBody SaleIngestRequest request) {
        return salesService.ingestPosSale(request);
    }

    /**
     * Generates fake sales records for testing before a real POS system is connected.
     */
    @PostMapping("/mock")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SaleRecordResponse> generateMockSales(@Valid @RequestBody MockSalesRequest request) {
        return salesService.generateMockSales(request);
    }

    /**
     * Returns sales records with optional date, source, and cashier filters.
     */
    @GetMapping
    public List<SaleRecordResponse> getSales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) SaleSource source,
            @RequestParam(required = false) Long cashierId) {
        return salesService.getSales(startDate, endDate, source, cashierId);
    }

    /**
     * Returns one sales record by id.
     */
    @GetMapping("/{id}")
    public SaleRecordResponse getSaleById(@PathVariable Long id) {
        return salesService.getSaleById(id);
    }

    /**
     * Returns today's sales amount and top-selling products for the dashboard.
     */
    @GetMapping("/dashboard")
    public SalesDashboardResponse getSalesDashboard() {
        return salesService.getSalesDashboard();
    }

    /**
     * Returns the top-selling products for a specific date range.
     */
    @GetMapping("/top-products")
    public List<TopSellingProductResponse> getTopSellingProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return salesService.getTopSellingProducts(startDate, endDate, limit);
    }
}
