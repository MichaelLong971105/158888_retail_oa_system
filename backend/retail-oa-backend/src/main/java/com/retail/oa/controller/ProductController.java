package com.retail.oa.controller;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-03-14 22:36
 **/

import com.retail.oa.dto.StockUpdateRequest;
import com.retail.oa.dto.product.ProductRequest;
import com.retail.oa.dto.product.ProductResponse;
import com.retail.oa.entity.InventoryLog;
import com.retail.oa.repository.InventoryLogRepository;
import com.retail.oa.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes product management and inventory log APIs.
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final InventoryLogRepository inventoryLogRepository;

    public ProductController(ProductService productService, InventoryLogRepository inventoryLogRepository) {
        this.productService = productService;
        this.inventoryLogRepository = inventoryLogRepository;
    }

    /**
     * Returns all products.
     */
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * Returns a single product by id.
     */
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    /**
     * Creates a new product.
     */
    @PostMapping
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    /**
     * Updates an existing product.
     */
    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    /**
     * Deletes a product by id.
     */
    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "Product deleted successfully";
    }

    /**
     * Increases stock for the specified product.
     */
    @PutMapping("/{id}/stock/in")
    public ProductResponse stockIn(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        return productService.stockIn(id, request.getQuantity(), request.getRemark());
    }

    /**
     * Decreases stock for the specified product.
     */
    @PutMapping("/{id}/stock/out")
    public ProductResponse stockOut(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        return productService.stockOut(id, request.getQuantity(), request.getRemark());
    }

    /**
     * Returns inventory log records for one product in descending creation order.
     */
    @GetMapping("/{id}/inventory-logs")
    public List<InventoryLog> getInventoryLogsByProductId(@PathVariable Long id) {
        return inventoryLogRepository.findByProductIdOrderByCreatedAtDesc(id);
    }
}
