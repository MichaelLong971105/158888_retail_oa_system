package com.retail.oa.entity;

/**
 * @program: retail-oa-backend
 * @description: test entity
 * @author: MichaelLong
 * @create: 2026-03-06 23:11
 **/

import jakarta.persistence.*;
@Entity
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

}