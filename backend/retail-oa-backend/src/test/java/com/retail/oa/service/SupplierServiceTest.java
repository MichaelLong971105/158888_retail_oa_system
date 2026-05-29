package com.retail.oa.service;

import com.retail.oa.entity.Supplier;
import com.retail.oa.exception.InvalidOperationException;
import com.retail.oa.repository.OrderRepository;
import com.retail.oa.repository.ProductRepository;
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
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    void deleteSupplierRejectsSupplierLinkedToProducts() {
        Supplier supplier = supplier(1L);

        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        when(productRepository.existsBySupplierId(supplier.getId())).thenReturn(true);

        assertThatThrownBy(() -> supplierService.deleteSupplier(supplier.getId()))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("linked to products");

        verify(supplierRepository, never()).delete(supplier);
    }

    @Test
    void deleteSupplierRejectsSupplierLinkedToOrders() {
        Supplier supplier = supplier(1L);

        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        when(productRepository.existsBySupplierId(supplier.getId())).thenReturn(false);
        when(orderRepository.existsBySupplierId(supplier.getId())).thenReturn(true);

        assertThatThrownBy(() -> supplierService.deleteSupplier(supplier.getId()))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("linked to orders");

        verify(supplierRepository, never()).delete(supplier);
    }

    private Supplier supplier(Long id) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setName("Supplier " + id);
        return supplier;
    }
}
