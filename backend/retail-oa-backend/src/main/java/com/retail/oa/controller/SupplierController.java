package com.retail.oa.controller;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-04-17 12:53
 **/

import com.retail.oa.dto.supplier.SupplierRequest;
import com.retail.oa.dto.supplier.SupplierResponse;
import com.retail.oa.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes supplier management APIs.
 */
@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * Creates a new supplier.
     */
    @PostMapping
    public SupplierResponse createSupplier(@Valid @RequestBody SupplierRequest request) {
        return supplierService.createSupplier(request);
    }

    /**
     * Returns all suppliers.
     */
    @GetMapping
    public List<SupplierResponse> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    /**
     * Returns one supplier by id.
     */
    @GetMapping("/{id}")
    public SupplierResponse getSupplierById(@PathVariable Long id) {
        return supplierService.getSupplierById(id);
    }

    /**
     * Updates an existing supplier.
     */
    @PutMapping("/{id}")
    public SupplierResponse updateSupplier(@PathVariable Long id,
                                           @Valid @RequestBody SupplierRequest request) {
        return supplierService.updateSupplier(id, request);
    }

    /**
     * Deletes a supplier by id.
     */
    @DeleteMapping("/{id}")
    public String deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return "Supplier deleted successfully";
    }
}
