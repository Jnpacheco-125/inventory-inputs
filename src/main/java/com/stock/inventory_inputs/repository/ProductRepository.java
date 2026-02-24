package com.stock.inventory_inputs.repository;

import com.stock.inventory_inputs.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);

    // Query to find products that can be manufactured with current stock
    @Query("SELECT p FROM Product p WHERE NOT EXISTS (" +
            "SELECT c FROM ProductComposition c WHERE c.product = p " +
            "AND c.requiredQuantity > (SELECT r.stockQuantity FROM RawMaterial r WHERE r = c.rawMaterial))")
    List<Product> findFeasibleProductsWithCurrentStock();
    @Query("""
       SELECT p FROM Product p
       LEFT JOIN FETCH p.composition c
       LEFT JOIN FETCH c.rawMaterial
       WHERE p.id = :id
       """)
    Optional<Product> findByIdWithComposition(@Param("id") Long id);
}
