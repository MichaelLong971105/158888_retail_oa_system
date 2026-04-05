package com.retail.oa.repository;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-03-14 22:33
 **/

import com.retail.oa.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);
}
