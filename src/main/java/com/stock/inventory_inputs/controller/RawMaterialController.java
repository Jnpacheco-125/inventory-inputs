package com.stock.inventory_inputs.controller;

import com.stock.inventory_inputs.dto.RawMaterialRequest;
import com.stock.inventory_inputs.model.RawMaterial;
import com.stock.inventory_inputs.repository.RawMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/raw-materials")
public class RawMaterialController {
    @Autowired
    private RawMaterialRepository rawMaterialRepository;

    // CREATE - Criar nova matéria-prima
    @PostMapping
    public ResponseEntity<RawMaterial> create(@RequestBody RawMaterialRequest request) {
        // Verificar se já existe com o mesmo código
        if (rawMaterialRepository.findByCode(request.code()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(null); // Código já existe
        }

        RawMaterial rawMaterial = new RawMaterial();
        rawMaterial.setCode(request.code());
        rawMaterial.setName(request.name());
        rawMaterial.setStockQuantity(request.stockQuantity());
        rawMaterial.setUnitOfMeasure(request.unitOfMeasure());

        RawMaterial saved = rawMaterialRepository.save(rawMaterial);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // READ - Listar todas as matérias-primas
    @GetMapping
    public List<RawMaterial> findAll() {
        return rawMaterialRepository.findAll();
    }

    // READ - Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<RawMaterial> findById(@PathVariable Long id) {
        return rawMaterialRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // READ - Buscar por código
    @GetMapping("/code/{code}")
    public ResponseEntity<RawMaterial> findByCode(@PathVariable String code) {
        return rawMaterialRepository.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE - Atualizar matéria-prima
    @PutMapping("/{id}")
    public ResponseEntity<RawMaterial> update(@PathVariable Long id,
                                              @RequestBody RawMaterialRequest request) {

        Optional<RawMaterial> optional = rawMaterialRepository.findById(id);

        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RawMaterial existing = optional.get();

        if (!existing.getCode().equals(request.code())) {
            if (rawMaterialRepository.findByCode(request.code()).isPresent()) {
                return ResponseEntity.badRequest().build();
            }
        }

        existing.setCode(request.code());
        existing.setName(request.name());
        existing.setStockQuantity(request.stockQuantity());
        existing.setUnitOfMeasure(request.unitOfMeasure());

        RawMaterial updated = rawMaterialRepository.save(existing);

        return ResponseEntity.ok(updated);
    }

    // DELETE - Deletar matéria-prima
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (rawMaterialRepository.existsById(id)) {
            rawMaterialRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // PATCH - Atualizar apenas o estoque (operação comum)
    @PatchMapping("/{id}/stock")
    public ResponseEntity<RawMaterial> updateStock(
            @PathVariable Long id,
            @RequestParam Double quantity,
            @RequestParam(defaultValue = "add") String operation) {

        Optional<RawMaterial> optional = rawMaterialRepository.findById(id);

        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RawMaterial material = optional.get();

        if (operation.equalsIgnoreCase("add")) {

            material.setStockQuantity(material.getStockQuantity() + quantity);

        } else if (operation.equalsIgnoreCase("remove")) {

            if (material.getStockQuantity() < quantity) {
                return ResponseEntity.badRequest().build();
            }

            material.setStockQuantity(material.getStockQuantity() - quantity);

        } else {
            return ResponseEntity.badRequest().build(); // operação inválida
        }

        RawMaterial updated = rawMaterialRepository.save(material);

        return ResponseEntity.ok(updated);
    }
}
