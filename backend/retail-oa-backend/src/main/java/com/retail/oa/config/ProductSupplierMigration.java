package com.retail.oa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Moves legacy product.supplier_id links into the product_suppliers join table.
 */
@Component
public class ProductSupplierMigration implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductSupplierMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public ProductSupplierMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            if (!tableExists("product_suppliers") || !columnExists("products", "supplier_id")) {
                return;
            }

            int migratedRows = jdbcTemplate.update("""
                    INSERT IGNORE INTO product_suppliers (product_id, supplier_id)
                    SELECT id, supplier_id
                    FROM products
                    WHERE supplier_id IS NOT NULL
                    """);

            jdbcTemplate.update("UPDATE products SET supplier_id = NULL WHERE supplier_id IS NOT NULL");

            if (migratedRows > 0) {
                LOGGER.info("Migrated {} legacy product supplier link(s)", migratedRows);
            }
        } catch (DataAccessException ex) {
            LOGGER.warn("Skipping legacy product supplier migration: {}", ex.getMessage());
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                """, Integer.class, tableName);

        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """, Integer.class, tableName, columnName);

        return count != null && count > 0;
    }
}
