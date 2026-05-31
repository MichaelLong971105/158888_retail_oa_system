package com.retail.oa.service;

import com.retail.oa.dto.OrderItemRequest;
import com.retail.oa.dto.OrderRequest;
import com.retail.oa.entity.InventoryLog;
import com.retail.oa.entity.Order;
import com.retail.oa.entity.OrderItem;
import com.retail.oa.entity.OrderStatus;
import com.retail.oa.entity.Product;
import com.retail.oa.entity.Supplier;
import com.retail.oa.repository.InventoryLogRepository;
import com.retail.oa.repository.OrderRepository;
import com.retail.oa.repository.ProductRepository;
import com.retail.oa.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryLogRepository inventoryLogRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderAllowsProductFromUnlinkedSupplier() {
        Supplier selectedSupplier = supplier(1L, "Selected Supplier");
        Product product = product(10L, "Tea", null, 20);

        OrderRequest request = new OrderRequest();
        request.setSupplierId(selectedSupplier.getId());
        request.setItems(List.of(orderItemRequest(product.getId(), 2)));

        when(supplierRepository.findById(selectedSupplier.getId())).thenReturn(Optional.of(selectedSupplier));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(100L);
            return savedOrder;
        });

        assertThat(orderService.createOrder(request))
                .satisfies(response -> {
                    assertThat(response.getSupplierId()).isEqualTo(selectedSupplier.getId());
                    assertThat(response.getItems()).hasSize(1);
                });
    }

    @Test
    void receivingPendingOrderIncreasesStockAndWritesInventoryLog() {
        Supplier supplier = supplier(1L, "Main Supplier");
        Product product = product(10L, "Coffee", supplier, 7);
        Order order = order(100L, supplier, product, 5, OrderStatus.PENDING);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(productRepository.save(product)).thenReturn(product);
        when(orderRepository.save(order)).thenReturn(order);

        orderService.updateOrderStatus(order.getId(), "RECEIVED");

        assertThat(product.getStock()).isEqualTo(12);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.RECEIVED);

        ArgumentCaptor<InventoryLog> logCaptor = ArgumentCaptor.forClass(InventoryLog.class);
        verify(inventoryLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getChangeType()).isEqualTo("IN");
        assertThat(logCaptor.getValue().getQuantity()).isEqualTo(5);
        assertThat(logCaptor.getValue().getRemark()).contains(order.getOrderNumber());
    }

    @Test
    void receivingPendingOrderAddsMissingSupplierToProduct() {
        Supplier supplier = supplier(1L, "Main Supplier");
        Product product = product(10L, "Coffee", null, 7);
        Order order = order(100L, supplier, product, 5, OrderStatus.PENDING);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(productRepository.save(product)).thenReturn(product);
        when(orderRepository.save(order)).thenReturn(order);

        orderService.updateOrderStatus(order.getId(), "RECEIVED");

        assertThat(product.getSuppliers())
                .extracting(Supplier::getId)
                .containsExactly(supplier.getId());
    }

    @Test
    void reapplyingReceivedStatusDoesNotIncreaseStockAgain() {
        Supplier supplier = supplier(1L, "Main Supplier");
        Product product = product(10L, "Coffee", supplier, 7);
        Order order = order(100L, supplier, product, 5, OrderStatus.RECEIVED);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.updateOrderStatus(order.getId(), "RECEIVED");

        assertThat(product.getStock()).isEqualTo(7);
        verify(productRepository, never()).save(any(Product.class));
        verify(inventoryLogRepository, never()).save(any(InventoryLog.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getAllOrdersNormalizesLegacyCompletedStatusToReceived() {
        Supplier supplier = supplier(1L, "Main Supplier");
        Product product = product(10L, "Coffee", supplier, 7);
        Order order = order(100L, supplier, product, 5, OrderStatus.COMPLETED);

        when(orderRepository.findAll()).thenReturn(List.of(order));

        assertThat(orderService.getAllOrders(null))
                .singleElement()
                .satisfies(response -> assertThat(response.getStatus()).isEqualTo("RECEIVED"));
    }

    @Test
    void receivedFilterIncludesLegacyCompletedOrders() {
        Supplier supplier = supplier(1L, "Main Supplier");
        Product product = product(10L, "Coffee", supplier, 7);
        Order completedOrder = order(100L, supplier, product, 5, OrderStatus.COMPLETED);

        when(orderRepository.findByStatusIn(List.of(OrderStatus.RECEIVED, OrderStatus.COMPLETED)))
                .thenReturn(List.of(completedOrder));

        assertThat(orderService.getAllOrders("RECEIVED"))
                .singleElement()
                .satisfies(response -> assertThat(response.getStatus()).isEqualTo("RECEIVED"));
    }

    private Supplier supplier(Long id, String name) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setName(name);
        return supplier;
    }

    private Product product(Long id, String name, Supplier supplier, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setSku("SKU-" + id);
        if (supplier != null) {
            product.getSuppliers().add(supplier);
        }
        product.setPrice(BigDecimal.valueOf(3.50));
        product.setStock(stock);
        return product;
    }

    private OrderItemRequest orderItemRequest(Long productId, int quantity) {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }

    private Order order(Long id, Supplier supplier, Product product, int quantity, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNumber("ORD-TEST");
        order.setSupplier(supplier);
        order.setStatus(status);
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));

        OrderItem item = new OrderItem();
        item.setId(200L);
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());
        order.setItems(List.of(item));
        return order;
    }
}
