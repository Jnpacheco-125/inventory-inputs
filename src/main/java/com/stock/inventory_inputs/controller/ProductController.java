package com.stock.inventory_inputs.controller;

import com.stock.inventory_inputs.dto.*;
import com.stock.inventory_inputs.model.Product;
import com.stock.inventory_inputs.model.RawMaterial;
import com.stock.inventory_inputs.repository.ProductRepository;
import com.stock.inventory_inputs.repository.RawMaterialRepository;
import com.stock.inventory_inputs.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.*;
import java.util.stream.Collectors;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    // ========== CRUD ==========

    /**
     * Criar novo produto
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductRequest request) {
        try {
            ProductResponseDTO created = productService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Listar todos os produtos
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> findAll() {
        List<ProductResponseDTO> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    /**
     * Buscar por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable Long id) {
        ProductResponseDTO product = productService.findById(id);
        return product != null
                ? ResponseEntity.ok(product)
                : ResponseEntity.notFound().build();
    }

    /**
     * Buscar por código
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ProductResponseDTO> findByCode(@PathVariable String code) {
        ProductResponseDTO product = productService.findByCode(code);
        return product != null
                ? ResponseEntity.ok(product)
                : ResponseEntity.notFound().build();
    }

    /**
     * Atualizar produto
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        try {
            ProductResponseDTO updated = productService.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Deletar produto
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            productService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== ENDPOINTS ADICIONAIS ==========

    /**
     * Ver composição de um produto (versão simplificada)
     */
    @GetMapping("/{id}/composition")
    public ResponseEntity<?> getComposition(@PathVariable Long id) {
        try {
            ProductResponseDTO product = productService.findById(id);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }

            // Formatar composição de forma legível
            List<Map<String, Object>> composition = product.composition().stream()
                    .map(comp -> Map.<String, Object>of(
                            "rawMaterialCode", comp.rawMaterialCode(),
                            "rawMaterialName", comp.rawMaterialName(),
                            "requiredQuantity", comp.requiredQuantity(),
                            "unitOfMeasure", comp.unitOfMeasure()
                    ))
                    .toList();

            return ResponseEntity.ok(composition);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Analisar um produto específico
     */
    @GetMapping("/{id}/analysis")
    public ResponseEntity<?> analyzeProduct(@PathVariable Long id) {
        try {
            ProductAnalysis analysis = productService.analyzeProduct(id);
            return ResponseEntity.ok(analysis);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

//    /**
//     * Otimizar produção - encontrar melhor produto
//     */
//    @GetMapping("/optimize")
//    public ResponseEntity<OptimizationResponse> optimizeProduction(
//            @RequestParam(defaultValue = "3") Integer limit) {
//        OptimizationResponse response = productService.optimizeProduction(limit);
//        return ResponseEntity.ok(response);
//    }

    /**
     * Verificar se produto é viável com estoque atual
     */
    @GetMapping("/{id}/feasible")
    public ResponseEntity<?> isProductFeasible(@PathVariable Long id) {
        try {
            boolean feasible = productService.isProductFeasible(id);
            return ResponseEntity.ok(Map.of(
                    "productId", id,
                    "feasible", feasible
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Listar apenas produtos viáveis
     */
    @GetMapping("/feasible")
    public ResponseEntity<List<ProductResponseDTO>> findFeasibleProducts() {
        List<ProductResponseDTO> products = productService.findFeasibleProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Calcular máximo de unidades possíveis
     */
    @GetMapping("/{id}/max-units")
    public ResponseEntity<?> calculateMaxUnits(@PathVariable Long id) {
        try {
            Integer maxUnits = productService.calculateMaxUnitsPossible(id);
            return ResponseEntity.ok(Map.of(
                    "productId", id,
                    "maxUnitsPossible", maxUnits
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Simular produção
     */
    @PostMapping("/{id}/simulate")
    public ResponseEntity<?> simulateProduction(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        try {
            Map<String, Object> simulation = productService.simulateProduction(id, quantity);
            return ResponseEntity.ok(simulation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Produzir (consumir matérias-primas)
     */
    @PostMapping("/{id}/produce")
    public ResponseEntity<?> produce(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        try {
            productService.produce(id, quantity);

            // Buscar produto atualizado
            ProductResponseDTO product = productService.findById(id);

            return ResponseEntity.ok(Map.of(
                    "message", "Produção realizada com sucesso",
                    "product", product.name(),
                    "quantity", quantity,
                    "totalProfit", product.profit() * quantity
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
