package com.stock.inventory_inputs.repository;

import com.stock.inventory_inputs.model.ProductComposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCompositionRepository  extends JpaRepository<ProductComposition, Long> {
    boolean existsByRawMaterialId(Long rawMaterialId);
}
