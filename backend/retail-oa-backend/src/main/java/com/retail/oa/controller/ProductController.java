package com.retail.oa.controller;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-03-14 22:36
 **/

import com.retail.oa.entity.Product;
import com.retail.oa.service.ProductService;
import com.retail.oa.dto.StockUpdateRequest;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.retail.oa.entity.InventoryLog;
import com.retail.oa.repository.InventoryLogRepository;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final InventoryLogRepository inventoryLogRepository;

    public ProductController(ProductService productService, InventoryLogRepository inventoryLogRepository) {
        this.productService = productService;
        this.inventoryLogRepository = inventoryLogRepository;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    public Product createProduct(@Valid @RequestBody Product product) {
        return productService.createProduct(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "Product deleted successfully";
    }

    @PutMapping("/{id}/stock/in")
    public Product stockIn(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        return productService.stockIn(id, request.getQuantity(), request.getRemark());
    }

    @PutMapping("/{id}/stock/out")
    public Product stockOut(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        return productService.stockOut(id, request.getQuantity(), request.getRemark());
    }

    @GetMapping("/{id}/inventory-logs")
    public List<InventoryLog> getInventoryLogsByProductId(@PathVariable Long id) {
        return inventoryLogRepository.findByProductIdOrderByCreatedAtDesc(id);
    }
}
