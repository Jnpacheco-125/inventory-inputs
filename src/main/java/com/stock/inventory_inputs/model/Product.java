package com.stock.inventory_inputs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Double profit;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductComposition> composition = new ArrayList<>();

    public void addRawMaterial(RawMaterial rawMaterial, Double quantity) {
        ProductComposition item = new ProductComposition();
        item.setProduct(this);
        item.setRawMaterial(rawMaterial);
        item.setRequiredQuantity(quantity);
        this.composition.add(item);
    }
}
