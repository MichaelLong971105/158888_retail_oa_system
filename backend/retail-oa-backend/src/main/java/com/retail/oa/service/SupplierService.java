package com.retail.oa.service;

import com.retail.oa.dto.supplier.SupplierRequest;
import com.retail.oa.dto.supplier.SupplierResponse;
import com.retail.oa.entity.Supplier;
import com.retail.oa.exception.DuplicateSupplierException;
import com.retail.oa.exception.InvalidOperationException;
import com.retail.oa.exception.SupplierNotFoundException;
import com.retail.oa.repository.OrderRepository;
import com.retail.oa.repository.ProductRepository;
import com.retail.oa.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles supplier CRUD operations and DTO mapping.
 */
@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public SupplierService(SupplierRepository supplierRepository,
                           ProductRepository productRepository,
                           OrderRepository orderRepository) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Creates a new supplier after validating name uniqueness.
     */
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByName(request.getName())) {
            throw new DuplicateSupplierException("Supplier name already exists: " + request.getName());
        }

        Supplier supplier = new Supplier();
        mapRequestToEntity(request, supplier);

        Supplier savedSupplier = supplierRepository.save(supplier);
        return mapToResponse(savedSupplier);
    }

    /**
     * Returns all suppliers as response DTOs.
     */
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns one supplier by id.
     */
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + id));
        return mapToResponse(supplier);
    }

    /**
     * Updates an existing supplier and preserves name uniqueness.
     */
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + id));

        if (!supplier.getName().equals(request.getName()) && supplierRepository.existsByName(request.getName())) {
            throw new DuplicateSupplierException("Supplier name already exists: " + request.getName());
        }

        mapRequestToEntity(request, supplier);

        Supplier updatedSupplier = supplierRepository.save(supplier);
        return mapToResponse(updatedSupplier);
    }

    /**
     * Deletes a supplier by id.
     */
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + id));

        if (productRepository.existsBySuppliers_Id(id)) {
            throw new InvalidOperationException("Supplier cannot be deleted because it is linked to products");
        }

        if (orderRepository.existsBySupplierId(id)) {
            throw new InvalidOperationException("Supplier cannot be deleted because it is linked to orders");
        }

        supplierRepository.delete(supplier);
    }

    /**
     * Copies request fields into the supplier entity.
     */
    private void mapRequestToEntity(SupplierRequest request, Supplier supplier) {
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setRemark(request.getRemark());
    }

    /**
     * Converts a supplier entity into the response DTO.
     */
    private SupplierResponse mapToResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        response.setId(supplier.getId());
        response.setName(supplier.getName());
        response.setContactPerson(supplier.getContactPerson());
        response.setPhone(supplier.getPhone());
        response.setEmail(supplier.getEmail());
        response.setAddress(supplier.getAddress());
        response.setRemark(supplier.getRemark());
        response.setCreatedAt(supplier.getCreatedAt());
        return response;
    }
}
