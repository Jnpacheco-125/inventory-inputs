package com.stock.inventory_inputs.controller;

import com.stock.inventory_inputs.dto.RawMaterialRequest;
import com.stock.inventory_inputs.dto.RawMaterialResponseDTO;
import com.stock.inventory_inputs.model.RawMaterial;
import com.stock.inventory_inputs.repository.RawMaterialRepository;
import com.stock.inventory_inputs.service.RawMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/raw-materials")
public class RawMaterialController {
    @Autowired
    private RawMaterialService rawMaterialService;

    // CREATE - Criar nova matéria-prima
    @PostMapping
    public ResponseEntity<?> create(@RequestBody RawMaterialRequest request) {
        try {
            RawMaterialRequest created = rawMaterialService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // READ - Listar todas as matérias-primas
    @GetMapping
    public ResponseEntity<List<RawMaterialResponseDTO>> findAll() {
        return ResponseEntity.ok(rawMaterialService.findAll());
    }

    // READ - Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        RawMaterialResponseDTO material = rawMaterialService.findById(id);

        if (material == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Matéria-prima não encontrada com ID: " + id));
        }

        return ResponseEntity.ok(material);
    }

    // READ - Buscar por código
    @GetMapping("/code/{code}")
    public ResponseEntity<?> findByCode(@PathVariable String code) {
        RawMaterialRequest material = rawMaterialService.findByCode(code);

        if (material == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Matéria-prima não encontrada"));
        }

        return ResponseEntity.ok(material);
    }

    // UPDATE - Atualizar matéria-prima
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody RawMaterialRequest request) {
        try {
            RawMaterialResponseDTO updated = rawMaterialService.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // DELETE - Deletar matéria-prima
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            rawMaterialService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // PATCH - Atualizar apenas o estoque (operação comum)

    @PatchMapping("/{id}/stock/add")
    public ResponseEntity<?> addToStock(@PathVariable Long id, @RequestParam Double quantity) {
        try {
            RawMaterialResponseDTO updated = rawMaterialService.addToStock(id, quantity);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/stock/remove")
    public ResponseEntity<?> removeFromStock(@PathVariable Long id, @RequestParam Double quantity) {
        try {
            RawMaterialResponseDTO updated = rawMaterialService.removeFromStock(id, quantity);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
