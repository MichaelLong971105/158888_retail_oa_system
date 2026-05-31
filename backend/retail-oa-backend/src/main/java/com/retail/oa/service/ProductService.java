package com.retail.oa.service;

import com.retail.oa.dto.product.ProductRequest;
import com.retail.oa.dto.product.ProductResponse;
import com.retail.oa.entity.InventoryLog;
import com.retail.oa.entity.Product;
import com.retail.oa.entity.Supplier;
import com.retail.oa.exception.DuplicateResourceException;
import com.retail.oa.exception.InsufficientStockException;
import com.retail.oa.exception.InvalidOperationException;
import com.retail.oa.exception.ResourceNotFoundException;
import com.retail.oa.exception.SupplierNotFoundException;
import com.retail.oa.repository.InventoryLogRepository;
import com.retail.oa.repository.OrderItemRepository;
import com.retail.oa.repository.ProductRepository;
import com.retail.oa.repository.SaleItemRepository;
import com.retail.oa.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles product CRUD and manual stock operations.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final SupplierRepository supplierRepository;
    private final OrderItemRepository orderItemRepository;
    private final SaleItemRepository saleItemRepository;

    public ProductService(ProductRepository productRepository,
                          InventoryLogRepository inventoryLogRepository,
                          SupplierRepository supplierRepository,
                          OrderItemRepository orderItemRepository,
                          SaleItemRepository saleItemRepository) {
        this.productRepository = productRepository;
        this.inventoryLogRepository = inventoryLogRepository;
        this.supplierRepository = supplierRepository;
        this.orderItemRepository = orderItemRepository;
        this.saleItemRepository = saleItemRepository;
    }

    /**
     * Returns all products.
     */
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns one product by id.
     */
    public ProductResponse getProductById(Long id) {
        return toResponse(getProductEntityById(id));
    }

    /**
     * Creates a new product after checking uniqueness rules.
     */
    public ProductResponse createProduct(ProductRequest request) {
        validateUniqueFields(request, null);

        Product product = new Product();
        applyRequest(product, request);

        return toResponse(productRepository.save(product));
    }

    /**
     * Updates an existing product while keeping uniqueness intact.
     */
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existingProduct = getProductEntityById(id);

        validateUniqueFields(request, id);
        applyRequest(existingProduct, request);

        return toResponse(productRepository.save(existingProduct));
    }

    /**
     * Deletes a product by id.
     */
    public void deleteProduct(Long id) {
        Product product = getProductEntityById(id);

        // Products are master data referenced by operational records, so delete only unused products.
        if (orderItemRepository.existsByProductId(id)) {
            throw new InvalidOperationException("Product cannot be deleted because it is linked to purchase orders");
        }

        if (saleItemRepository.existsByProductId(id)) {
            throw new InvalidOperationException("Product cannot be deleted because it is linked to sales records");
        }

        if (inventoryLogRepository.existsByProductId(id)) {
            throw new InvalidOperationException("Product cannot be deleted because it has inventory logs");
        }

        productRepository.delete(product);
    }

    /**
     * Increases product stock and writes an IN inventory log.
     */
    public ProductResponse stockIn(Long id, Integer quantity, String remark) {
        Product product = getProductEntityById(id);

        product.setStock(product.getStock() + quantity);
        Product updatedProduct = productRepository.save(product);

        InventoryLog log = new InventoryLog();
        log.setProduct(updatedProduct);
        log.setChangeType("IN");
        log.setQuantity(quantity);
        log.setRemark(remark);

        inventoryLogRepository.save(log);

        return toResponse(updatedProduct);
    }

    /**
     * Decreases product stock and writes an OUT inventory log.
     */
    public ProductResponse stockOut(Long id, Integer quantity, String remark) {
        Product product = getProductEntityById(id);

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

        return toResponse(updatedProduct);
    }

    private Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private void validateUniqueFields(ProductRequest request, Long currentProductId) {
        // During updates, the current product may keep its own SKU or barcode without counting as a duplicate.
        Product productBySku = productRepository.findBySku(request.getSku().trim()).orElse(null);
        if (productBySku != null && !productBySku.getId().equals(currentProductId)) {
            throw new DuplicateResourceException("SKU already exists");
        }

        String barcode = normalizeNullable(request.getBarcode());
        if (barcode != null) {
            Product productByBarcode = productRepository.findByBarcode(barcode).orElse(null);
            if (productByBarcode != null && !productByBarcode.getId().equals(currentProductId)) {
                throw new DuplicateResourceException("Barcode already exists");
            }
        }
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.getName().trim());
        product.setSku(request.getSku().trim());
        product.setBarcode(normalizeNullable(request.getBarcode()));
        product.setCategory(normalizeNullable(request.getCategory()));
        product.setBrand(normalizeNullable(request.getBrand()));
        product.setSpecification(normalizeNullable(request.getSpecification()));
        product.setUnit(request.getUnit().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setMinStock(request.getMinStock());
        product.setStatus(request.getStatus());
        product.setDescription(normalizeNullable(request.getDescription()));
        product.setSuppliers(resolveSuppliers(request));
    }

    private Set<Supplier> resolveSuppliers(ProductRequest request) {
        LinkedHashSet<Long> requestedIds = getRequestedSupplierIds(request);
        if (requestedIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<Supplier> suppliers = supplierRepository.findAllById(requestedIds);
        Map<Long, Supplier> suppliersById = suppliers.stream()
                .collect(Collectors.toMap(Supplier::getId, Function.identity()));

        for (Long supplierId : requestedIds) {
            if (!suppliersById.containsKey(supplierId)) {
                throw new SupplierNotFoundException("Supplier not found with id: " + supplierId);
            }
        }

        return requestedIds.stream()
                .map(suppliersById::get)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private LinkedHashSet<Long> getRequestedSupplierIds(ProductRequest request) {
        LinkedHashSet<Long> supplierIds = new LinkedHashSet<>();

        if (request.getSupplierIds() != null) {
            request.getSupplierIds().stream()
                    .filter(id -> id != null)
                    .forEach(supplierIds::add);
        } else if (request.getSupplierId() != null) {
            supplierIds.add(request.getSupplierId());
        }

        return supplierIds;
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setBarcode(product.getBarcode());
        response.setCategory(product.getCategory());
        response.setBrand(product.getBrand());
        response.setSpecification(product.getSpecification());
        response.setUnit(product.getUnit());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setMinStock(product.getMinStock());
        response.setStatus(product.getStatus());
        response.setDescription(product.getDescription());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        List<Supplier> suppliers = sortedSuppliers(product);
        List<Long> supplierIds = suppliers.stream()
                .map(Supplier::getId)
                .collect(Collectors.toList());
        List<String> supplierNames = suppliers.stream()
                .map(Supplier::getName)
                .collect(Collectors.toList());

        response.setSupplierIds(supplierIds);
        response.setSupplierNames(supplierNames);

        if (!suppliers.isEmpty()) {
            response.setSupplierId(suppliers.get(0).getId());
            response.setSupplierName(String.join(", ", supplierNames));
        }

        return response;
    }

    private List<Supplier> sortedSuppliers(Product product) {
        if (product.getSuppliers() == null) {
            return List.of();
        }

        return product.getSuppliers().stream()
                .sorted(Comparator
                        .comparing((Supplier supplier) -> supplier.getName() == null ? "" : supplier.getName(),
                                String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Supplier::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
