package com.retail.oa.entity;

import jakarta.persistence.*;

/**
 * Simple test entity kept for basic persistence checks.
 */
@Entity
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

}
