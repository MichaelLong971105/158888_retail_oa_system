package com.retail.oa.service;

import com.retail.oa.dto.sale.MockSalesRequest;
import com.retail.oa.dto.sale.SaleIngestRequest;
import com.retail.oa.dto.sale.SaleItemRequest;
import com.retail.oa.dto.sale.SaleItemResponse;
import com.retail.oa.dto.sale.SaleRecordResponse;
import com.retail.oa.dto.sale.SalesDashboardResponse;
import com.retail.oa.dto.sale.TopSellingProductResponse;
import com.retail.oa.entity.InventoryLog;
import com.retail.oa.entity.Product;
import com.retail.oa.entity.ProductStatus;
import com.retail.oa.entity.SaleItem;
import com.retail.oa.entity.SaleRecord;
import com.retail.oa.entity.SaleSource;
import com.retail.oa.entity.User;
import com.retail.oa.entity.UserRole;
import com.retail.oa.exception.InsufficientStockException;
import com.retail.oa.exception.InvalidOperationException;
import com.retail.oa.exception.ResourceNotFoundException;
import com.retail.oa.repository.InventoryLogRepository;
import com.retail.oa.repository.ProductRepository;
import com.retail.oa.repository.SaleItemRepository;
import com.retail.oa.repository.SaleRecordRepository;
import com.retail.oa.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Handles POS-ready sale ingestion, manual mock generation, and sales analytics.
 */
@Service
public class SalesService {

    private final SaleRecordRepository saleRecordRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final Random random = new Random();

    public SalesService(SaleRecordRepository saleRecordRepository,
                        SaleItemRepository saleItemRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        InventoryLogRepository inventoryLogRepository) {
        this.saleRecordRepository = saleRecordRepository;
        this.saleItemRepository = saleItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.inventoryLogRepository = inventoryLogRepository;
    }

    @Transactional
    public SaleRecordResponse createManualSale(SaleIngestRequest request) {
        return createSaleInternal(request, SaleSource.MANUAL);
    }

    @Transactional
    public SaleRecordResponse ingestPosSale(SaleIngestRequest request) {
        return createSaleInternal(request, SaleSource.POS);
    }

    @Transactional
    public List<SaleRecordResponse> generateMockSales(MockSalesRequest request) {
        // Mock sales should exercise the same stock movement path as real sales so dashboards stay honest.
        List<Product> availableProducts = productRepository.findByStatusAndStockGreaterThan(ProductStatus.ACTIVE, 0);
        if (availableProducts.isEmpty()) {
            throw new InvalidOperationException("At least one active product with stock is required to generate mock sales");
        }

        List<User> enabledCashiers = userRepository.findByEnabledTrue();
        enabledCashiers.removeIf(user -> user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.MANAGER && user.getRole() != UserRole.STAFF);
        if (enabledCashiers.isEmpty()) {
            throw new InvalidOperationException("At least one enabled user is required to generate mock sales");
        }

        LocalDate saleDate = request.getSaleDate() == null ? LocalDate.now() : request.getSaleDate();
        List<SaleRecordResponse> generated = new ArrayList<>();

        for (int i = 0; i < request.getCount(); i++) {
            User cashier = request.getCashierUserId() != null
                    ? userRepository.findById(request.getCashierUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cashier not found with id: " + request.getCashierUserId()))
                    : enabledCashiers.get(random.nextInt(enabledCashiers.size()));

            List<Product> selectableProducts = productRepository.findByStatusAndStockGreaterThan(ProductStatus.ACTIVE, 0);
            if (selectableProducts.isEmpty()) {
                break;
            }

            Collections.shuffle(selectableProducts, random);
            int itemCount = Math.min(selectableProducts.size(), 1 + random.nextInt(Math.min(3, selectableProducts.size())));
            List<SaleItemRequest> itemRequests = new ArrayList<>();

            for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
                Product product = selectableProducts.get(itemIndex);
                SaleItemRequest itemRequest = new SaleItemRequest();
                itemRequest.setProductId(product.getId());
                itemRequest.setQuantity(1 + random.nextInt(Math.min(3, Math.max(1, product.getStock()))));
                itemRequest.setUnitPrice(product.getPrice());
                itemRequests.add(itemRequest);
            }

            SaleIngestRequest saleRequest = new SaleIngestRequest();
            saleRequest.setSource(SaleSource.MOCK);
            saleRequest.setCashierUserId(cashier.getId());
            saleRequest.setCashierName(cashier.getUsername());
            saleRequest.setSaleTime(randomSaleTime(saleDate));
            saleRequest.setRemark("Generated mock sale for testing");
            saleRequest.setItems(itemRequests);

            generated.add(createSaleInternal(saleRequest, SaleSource.MOCK));
        }

        return generated;
    }

    @Transactional
    public List<SaleRecordResponse> getSales(LocalDate startDate, LocalDate endDate, SaleSource source, Long cashierId) {
        List<SaleRecord> records;

        // Prefer repository-level filters for the primary selector, then apply optional cross-filters in memory.
        if (source != null) {
            records = saleRecordRepository.findBySourceOrderBySaleTimeDesc(source);
        } else if (cashierId != null) {
            records = saleRecordRepository.findByCashierIdOrderBySaleTimeDesc(cashierId);
        } else if (startDate != null || endDate != null) {
            LocalDate start = startDate == null ? LocalDate.now().minusDays(30) : startDate;
            LocalDate end = endDate == null ? start : endDate;
            validateDateRange(start, end);
            records = saleRecordRepository.findBySaleTimeBetweenOrderBySaleTimeDesc(start.atStartOfDay(), end.atTime(LocalTime.MAX));
        } else {
            records = saleRecordRepository.findAllByOrderBySaleTimeDesc();
        }

        if (source != null && (startDate != null || endDate != null)) {
            LocalDate start = startDate == null ? LocalDate.now().minusDays(30) : startDate;
            LocalDate end = endDate == null ? start : endDate;
            validateDateRange(start, end);
            LocalDateTime startDateTime = start.atStartOfDay();
            LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
            records.removeIf(record -> record.getSaleTime().isBefore(startDateTime) || record.getSaleTime().isAfter(endDateTime));
        }

        if (cashierId != null && (source != null || startDate != null || endDate != null)) {
            records.removeIf(record -> record.getCashier() == null || !cashierId.equals(record.getCashier().getId()));
        }

        List<SaleRecordResponse> responses = new ArrayList<>();
        for (SaleRecord record : records) {
            responses.add(toResponse(record));
        }

        return responses;
    }

    @Transactional
    public SaleRecordResponse getSaleById(Long id) {
        SaleRecord saleRecord = saleRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale record not found with id: " + id));

        return toResponse(saleRecord);
    }

    public SalesDashboardResponse getSalesDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        SalesDashboardResponse response = new SalesDashboardResponse();
        response.setTodaySalesAmount(saleRecordRepository.sumTotalAmountBetween(start, end));
        response.setTodaySalesCount(saleRecordRepository.countBySaleTimeBetween(start, end));
        response.setTopSellingProducts(saleItemRepository.findTopSellingProductsBetween(start, end, PageRequest.of(0, 10)));
        return response;
    }

    public List<TopSellingProductResponse> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 50);
        LocalDate start = startDate == null ? LocalDate.now() : startDate;
        LocalDate end = endDate == null ? start : endDate;
        validateDateRange(start, end);

        return saleItemRepository.findTopSellingProductsBetween(
                start.atStartOfDay(),
                end.atTime(LocalTime.MAX),
                PageRequest.of(0, safeLimit)
        );
    }

    @Transactional
    protected SaleRecordResponse createSaleInternal(SaleIngestRequest request, SaleSource defaultSource) {
        SaleSource source = request.getSource() == null ? defaultSource : request.getSource();
        LocalDateTime saleTime = request.getSaleTime() == null ? LocalDateTime.now() : request.getSaleTime();

        // POS payloads may carry only a cashier name, while manual entries usually link to a system user.
        User cashier = resolveCashier(request.getCashierUserId());
        String cashierName = resolveCashierName(request.getCashierName(), cashier);

        SaleRecord saleRecord = new SaleRecord();
        saleRecord.setSaleNumber(generateSaleNumber());
        saleRecord.setExternalSaleId(normalizeNullable(request.getExternalSaleId()));
        saleRecord.setSource(source);
        saleRecord.setCashier(cashier);
        saleRecord.setCashierName(cashierName);
        saleRecord.setSaleTime(saleTime);
        saleRecord.setRemark(normalizeNullable(request.getRemark()));

        List<SaleItem> saleItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SaleItemRequest itemRequest : request.getItems()) {
            Product product = resolveProduct(itemRequest);
            BigDecimal unitPrice = resolveUnitPrice(itemRequest, product);
            int quantity = itemRequest.getQuantity() == null ? 0 : itemRequest.getQuantity();
            if (quantity <= 0) {
                throw new InvalidOperationException("Sale item quantity must be greater than 0");
            }

            String productName = product != null ? product.getName() : normalizeNullable(itemRequest.getProductName());
            if (productName == null) {
                throw new InvalidOperationException("Product name cannot be empty when the product cannot be resolved");
            }

            BigDecimal lineAmount = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);

            SaleItem saleItem = new SaleItem();
            saleItem.setSaleRecord(saleRecord);
            saleItem.setProduct(product);
            saleItem.setProductName(productName);
            saleItem.setProductSku(product != null ? product.getSku() : normalizeNullable(itemRequest.getSku()));
            saleItem.setQuantity(quantity);
            saleItem.setUnitPrice(unitPrice);
            saleItem.setLineAmount(lineAmount);

            saleItems.add(saleItem);
            totalAmount = totalAmount.add(lineAmount);

            if (product != null) {
                // Unknown external products are recorded for reporting, but only matched products affect stock.
                registerInventoryOut(product, quantity, saleRecord.getSaleNumber(), saleTime);
            }
        }

        saleRecord.setItems(saleItems);
        saleRecord.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));

        SaleRecord savedRecord = saleRecordRepository.save(saleRecord);
        return toResponse(savedRecord);
    }

    private Product resolveProduct(SaleItemRequest itemRequest) {
        Product product = null;
        if (itemRequest.getProductId() != null) {
            product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemRequest.getProductId()));
        } else if (normalizeNullable(itemRequest.getSku()) != null) {
            product = productRepository.findBySku(itemRequest.getSku().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with sku: " + itemRequest.getSku()));
        }

        // Selling discontinued or inactive stock would make inventory counts look valid when operations should stop.
        if (product != null && product.getStatus() != ProductStatus.ACTIVE) {
            throw new InvalidOperationException("Only active products can be sold: " + product.getName());
        }

        return product;
    }

    private BigDecimal resolveUnitPrice(SaleItemRequest itemRequest, Product product) {
        BigDecimal unitPrice = itemRequest.getUnitPrice();
        if (unitPrice == null && product != null) {
            unitPrice = product.getPrice();
        }

        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Unit price must be greater than 0");
        }

        return unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    private User resolveCashier(Long cashierUserId) {
        if (cashierUserId == null) {
            return null;
        }

        User cashier = userRepository.findById(cashierUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Cashier not found with id: " + cashierUserId));

        if (!cashier.isEnabled()) {
            throw new InvalidOperationException("Disabled users cannot be assigned as cashier");
        }

        return cashier;
    }

    private String resolveCashierName(String requestedName, User cashier) {
        String normalizedName = normalizeNullable(requestedName);
        if (normalizedName != null) {
            return normalizedName;
        }
        if (cashier != null) {
            return cashier.getUsername();
        }
        throw new InvalidOperationException("Cashier name is required when no cashier account is linked");
    }

    private void registerInventoryOut(Product product, int quantity, String saleNumber, LocalDateTime saleTime) {
        if (product.getStock() < quantity) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        // Sales are the outbound inventory path; every stock deduction gets a matching audit log.
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        InventoryLog inventoryLog = new InventoryLog();
        inventoryLog.setProduct(product);
        inventoryLog.setChangeType("OUT");
        inventoryLog.setQuantity(quantity);
        inventoryLog.setRemark("Sale " + saleNumber + " at " + saleTime);

        inventoryLogRepository.save(inventoryLog);
    }

    private SaleRecordResponse toResponse(SaleRecord saleRecord) {
        SaleRecordResponse response = new SaleRecordResponse();
        response.setId(saleRecord.getId());
        response.setSaleNumber(saleRecord.getSaleNumber());
        response.setExternalSaleId(saleRecord.getExternalSaleId());
        response.setSource(saleRecord.getSource());
        response.setTotalAmount(saleRecord.getTotalAmount());
        response.setSaleTime(saleRecord.getSaleTime());
        response.setCreatedAt(saleRecord.getCreatedAt());
        response.setRemark(saleRecord.getRemark());
        response.setCashierName(saleRecord.getCashierName());

        if (saleRecord.getCashier() != null) {
            response.setCashierId(saleRecord.getCashier().getId());
        }

        List<SaleItemResponse> itemResponses = new ArrayList<>();
        for (SaleItem item : saleRecord.getItems()) {
            SaleItemResponse itemResponse = new SaleItemResponse();
            itemResponse.setId(item.getId());
            itemResponse.setProductName(item.getProductName());
            itemResponse.setProductSku(item.getProductSku());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setUnitPrice(item.getUnitPrice());
            itemResponse.setLineAmount(item.getLineAmount());
            if (item.getProduct() != null) {
                itemResponse.setProductId(item.getProduct().getId());
            }
            itemResponses.add(itemResponse);
        }

        response.setItems(itemResponses);
        return response;
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new InvalidOperationException("Start date cannot be after end date");
        }
    }

    private String generateSaleNumber() {
        return "SAL-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private LocalDateTime randomSaleTime(LocalDate date) {
        return date.atStartOfDay()
                .plusHours(random.nextInt(14))
                .plusMinutes(random.nextInt(60))
                .plusSeconds(random.nextInt(60));
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
