package com.stock.inventory_inputs.repository;

import com.stock.inventory_inputs.model.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {
    Optional<RawMaterial> findByCode(String code);
}
