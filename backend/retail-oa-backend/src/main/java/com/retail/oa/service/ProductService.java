package com.retail.oa.service;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-03-14 22:35
 **/

import com.retail.oa.entity.InventoryLog;
import com.retail.oa.entity.Product;
import com.retail.oa.exception.DuplicateResourceException;
import com.retail.oa.exception.InsufficientStockException;
import com.retail.oa.exception.ResourceNotFoundException;
import com.retail.oa.repository.InventoryLogRepository;
import com.retail.oa.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;

    public ProductService(ProductRepository productRepository,
                          InventoryLogRepository inventoryLogRepository) {
        this.productRepository = productRepository;
        this.inventoryLogRepository = inventoryLogRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Product createProduct(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new DuplicateResourceException("SKU already exists");
        }
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (!existingProduct.getSku().equals(productDetails.getSku())
                && productRepository.existsBySku(productDetails.getSku())) {
            throw new DuplicateResourceException("SKU already exists");
        }

        existingProduct.setName(productDetails.getName());
        existingProduct.setSku(productDetails.getSku());
        existingProduct.setCategory(productDetails.getCategory());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setStock(productDetails.getStock());
        existingProduct.setDescription(productDetails.getDescription());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productRepository.delete(existingProduct);
    }

    public Product stockIn(Long id, Integer quantity, String remark) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setStock(product.getStock() + quantity);
        Product updatedProduct = productRepository.save(product);

        InventoryLog log = new InventoryLog();
        log.setProduct(updatedProduct);
        log.setChangeType("IN");
        log.setQuantity(quantity);
        log.setRemark(remark);

        inventoryLogRepository.save(log);

        return updatedProduct;
    }

    public Product stockOut(Long id, Integer quantity, String remark) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (product.getStock() < quantity) {
            throw new InsufficientStockException("Insufficient stock for product id: " + id);
        }

        product.setStock(product.getStock() - quantity);
        Product updatedProduct = productRepository.save(product);

        InventoryLog log = new InventoryLog();
        log.setProduct(updatedProduct);
        log.setChangeType("OUT");
        log.setQuantity(quantity);
        log.setRemark(remark);

        inventoryLogRepository.save(log);

        return updatedProduct;
    }
}
