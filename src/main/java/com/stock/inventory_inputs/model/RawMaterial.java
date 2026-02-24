package com.stock.inventory_inputs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "raw_materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;  // Material code

    @Column(nullable = false)
    private String name;  // Material name

    @Column(nullable = false)
    private Double stockQuantity; // Quantity in stock

    @Column(nullable = false)
    private String unitOfMeasure; // e.g., "g", "kg", "unit", "ml", "L"
}
