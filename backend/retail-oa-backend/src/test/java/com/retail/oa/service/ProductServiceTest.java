package com.retail.oa.service;

import com.retail.oa.entity.Product;
import com.retail.oa.exception.InvalidOperationException;
import com.retail.oa.repository.InventoryLogRepository;
import com.retail.oa.repository.OrderItemRepository;
import com.retail.oa.repository.ProductRepository;
import com.retail.oa.repository.SaleItemRepository;
import com.retail.oa.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryLogRepository inventoryLogRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private SaleItemRepository saleItemRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void deleteProductRejectsProductLinkedToPurchaseOrders() {
        Product product = product(1L);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(product.getId())).thenReturn(true);

        assertThatThrownBy(() -> productService.deleteProduct(product.getId()))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("purchase orders");

        verify(productRepository, never()).delete(product);
    }

    @Test
    void deleteProductRejectsProductWithInventoryLogs() {
        Product product = product(1L);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderItemRepository.existsByProductId(product.getId())).thenReturn(false);
        when(saleItemRepository.existsByProductId(product.getId())).thenReturn(false);
        when(inventoryLogRepository.existsByProductId(product.getId())).thenReturn(true);

        assertThatThrownBy(() -> productService.deleteProduct(product.getId()))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("inventory logs");

        verify(productRepository, never()).delete(product);
    }

    private Product product(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Product " + id);
        return product;
    }
}
