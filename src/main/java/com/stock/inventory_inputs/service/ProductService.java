package com.stock.inventory_inputs.service;

import com.stock.inventory_inputs.dto.*;
import com.stock.inventory_inputs.model.Product;
import com.stock.inventory_inputs.model.ProductComposition;
import com.stock.inventory_inputs.model.RawMaterial;
import com.stock.inventory_inputs.repository.ProductRepository;
import com.stock.inventory_inputs.repository.RawMaterialRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RawMaterialRepository rawMaterialRepository;

    /**
     * Converte Entidade Product para DTO (para retorno)
     * Nota: Como você não tem ProductResponseDTO, estou usando ProductRequest
     * mas idealmente você criaria um ProductResponseDTO
     */
    private ProductRequest toDTO(Product product) {
        List<CompositionItem> composition = product.getComposition().stream()
                .map(comp -> new CompositionItem(
                        comp.getRawMaterial().getCode(),
                        comp.getRequiredQuantity()
                ))
                .collect(Collectors.toList());

        return new ProductRequest(
                product.getCode(),
                product.getName(),
                product.getPrice(),
                product.getProfit(),
                composition
        );
    }

    /**
     * Converte DTO para Entidade Product
     */
    private Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setCode(request.code());
        product.setName(request.name());
        product.setPrice(request.price());
        product.setProfit(request.profit());
        return product;
    }
    // ADICIONE ESTE MÉTODO NOVO (pode colocar perto dos outros métodos de conversão)
    private ProductResponseDTO toResponseDTO(Product product) {
        List<ProductCompositionDTO> composition = product.getComposition().stream()
                .map(comp -> new ProductCompositionDTO(
                        comp.getRawMaterial().getId(),
                        comp.getRawMaterial().getCode(),
                        comp.getRawMaterial().getName(),
                        comp.getRequiredQuantity(),
                        comp.getRawMaterial().getUnitOfMeasure()
                ))
                .collect(Collectors.toList());

        return new ProductResponseDTO(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getPrice(),
                product.getProfit(),
                composition
        );
    }

    /**
     * Listar todos os produtos
     */
    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll().stream()
                .map(this::toResponseDTO)  // ← só muda isso
                .collect(Collectors.toList());
    }

    /**
     * Buscar produto por ID
     */
    public ProductResponseDTO findById(Long id) {
        return productRepository.findById(id)
                .map(this::toResponseDTO)  // ← só muda isso
                .orElse(null);
    }

    /**
     * Buscar produto por código
     */
    public ProductResponseDTO findByCode(String code) {
        return productRepository.findByCode(code)
                .map(this::toResponseDTO)  // ← só muda isso
                .orElse(null);
    }

    /**
     * Criar novo produto
     */
    @Transactional
    public ProductResponseDTO create(ProductRequest request)  {
        // Verificar se código já existe
        if (productRepository.findByCode(request.code()).isPresent()) {
            throw new RuntimeException("Já existe um produto com o código: " + request.code());
        }

        // Validar dados
        validateProduct(request);

        // Criar produto básico
        Product product = toEntity(request);
        Product savedProduct = productRepository.save(product);

        // Adicionar composição
        addCompositionToProduct(savedProduct, request.composition());

        // Salvar com composição
        savedProduct = productRepository.save(savedProduct);
        return toResponseDTO(savedProduct);  // ← ANTES era: return toDTO(savedProduct)
    }

    /**
     * Atualizar produto
     */
    @Transactional
    public ProductResponseDTO update(Long id, ProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));

        // Verificar se o novo código já existe
        if (!existing.getCode().equals(request.code())) {
            if (productRepository.findByCode(request.code()).isPresent()) {
                throw new RuntimeException("Já existe um produto com o código: " + request.code());
            }
        }

        // Validar dados
        validateProduct(request);

        // Atualizar dados básicos
        existing.setCode(request.code());
        existing.setName(request.name());
        existing.setPrice(request.price());
        existing.setProfit(request.profit());

        // Limpar composição antiga
        existing.getComposition().clear();

        // Adicionar nova composição
        addCompositionToProduct(existing, request.composition());

        Product updated = productRepository.save(existing);
        return toResponseDTO(updated);  // ← ANTES era: return toDTO(updated)
    }

    // ADICIONE ESTE MÉTODO NOVO (se quiser)
    public List<ProductResponseDTO> findFeasibleProducts() {
        return productRepository.findAll().stream()
                .filter(p -> isProductFeasible(p.getId()))
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
    /**
     * Deletar produto
     */
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Produto não encontrado com ID: " + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Validar dados do produto
     */
    private void validateProduct(ProductRequest request) {
        if (request.price() <= 0) {
            throw new RuntimeException("Preço deve ser maior que zero");
        }
        if (request.profit() < 0) {
            throw new RuntimeException("Lucro não pode ser negativo");
        }
        if (request.composition() == null || request.composition().isEmpty()) {
            throw new RuntimeException("Produto deve ter pelo menos um item na composição");
        }
    }

    /**
     * Adicionar composição ao produto
     */
    private void addCompositionToProduct(Product product, List<CompositionItem> composition) {
        for (CompositionItem item : composition) {
            if (item.requiredQuantity() <= 0) {
                throw new RuntimeException("Quantidade necessária deve ser maior que zero");
            }

            RawMaterial material = rawMaterialRepository.findByCode(item.rawMaterialCode())
                    .orElseThrow(() -> new RuntimeException(
                            "Matéria-prima não encontrada: " + item.rawMaterialCode()));

            product.addRawMaterial(material, item.requiredQuantity());
        }
    }

    /**
     * Calcular máximo de unidades possíveis
     */
    public Integer calculateMaxUnitsPossible(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Integer maxUnits = Integer.MAX_VALUE;

        for (ProductComposition comp : product.getComposition()) {
            RawMaterial material = comp.getRawMaterial();
            Double requiredPerUnit = comp.getRequiredQuantity();
            Double available = material.getStockQuantity();

            if (requiredPerUnit > 0) {
                Integer possibleUnits = (int) Math.floor(available / requiredPerUnit);
                if (possibleUnits < maxUnits) {
                    maxUnits = possibleUnits;
                }
            }
        }

        return maxUnits == Integer.MAX_VALUE ? 0 : maxUnits;
    }

    /**
     * Analisar um produto específico
     */

    public ProductAnalysis analyzeProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Integer maxUnits = calculateMaxUnitsPossible(productId);
        Double totalProfit = product.getProfit() * maxUnits;

        // Encontrar material limitante
        String limitingMaterial = "";
        Integer minUnits = Integer.MAX_VALUE;

        for (ProductComposition comp : product.getComposition()) {
            RawMaterial material = comp.getRawMaterial();
            Integer possibleUnits = (int) Math.floor(
                    material.getStockQuantity() / comp.getRequiredQuantity()
            );

            if (possibleUnits < minUnits) {
                minUnits = possibleUnits;
                limitingMaterial = material.getName();
            }
        }
        Map<String, Double> materialsRequired = new HashMap<>();

        for (ProductComposition comp : product.getComposition()) {
            materialsRequired.put(
                    comp.getRawMaterial().getName(),
                    comp.getRequiredQuantity()
            );
        }
        return new ProductAnalysis(
                productId,
                product.getName(),
                product.getProfit(),
                maxUnits,
                totalProfit,
                limitingMaterial,
                materialsRequired
        );
    }

    /**
     * Otimizar produção - encontrar melhor produto
     */
    public OptimizationResponse optimizeProduction() {

        List<Product> allProducts = productRepository.findAll();
        List<ProductAnalysis> analyses = new ArrayList<>();

        for (Product product : allProducts) {

            Integer maxUnits = calculateMaxUnitsPossible(product.getId());
            Double totalProfit = product.getProfit() * maxUnits;

            String limitingMaterial = "";
            Integer minUnits = Integer.MAX_VALUE;

            Map<String, Double> materialsRequired = new HashMap<>();

            for (ProductComposition comp : product.getComposition()) {

                RawMaterial material = comp.getRawMaterial();

                Integer possibleUnits = (int) Math.floor(
                        material.getStockQuantity() / comp.getRequiredQuantity()
                );

                if (possibleUnits < minUnits) {
                    minUnits = possibleUnits;
                    limitingMaterial = material.getName();
                }

                materialsRequired.put(
                        material.getName(),
                        comp.getRequiredQuantity()
                );
            }

            analyses.add(new ProductAnalysis(
                    product.getId(),
                    product.getName(),
                    product.getProfit(),
                    maxUnits,
                    totalProfit,
                    limitingMaterial,
                    materialsRequired
            ));
        }

        // Ordenar por maior lucro total
        analyses.sort((a, b) -> b.totalPossibleProfit()
                .compareTo(a.totalPossibleProfit()));

        // 🔥 Preparar dados do melhor produto
        String bestProductName = null;
        Integer bestProductQuantity = null;
        Double bestProductTotalProfit = null;
        String limitingMaterial = null;
        Map<String, Double> materialsUsed = new HashMap<>();
        Map<String, Double> materialsRemaining = new HashMap<>();
        String recommendation;

        if (analyses.isEmpty()) {

            recommendation = "Não há produtos cadastrados";

        } else {

            ProductAnalysis best = analyses.get(0);

            bestProductName = best.name();
            bestProductQuantity = best.maxPossibleUnits();
            bestProductTotalProfit = best.totalPossibleProfit();
            limitingMaterial = best.limitingMaterial();

            // 🔥 Buscar o produto real pelo ID (mais eficiente)
            Product bestProduct = allProducts.stream()
                    .filter(p -> p.getId().equals(best.id()))
                    .findFirst()
                    .orElseThrow();

            // 🔹 Calcular materiais usados e restantes corretamente
            for (ProductComposition comp : bestProduct.getComposition()) {

                RawMaterial material = comp.getRawMaterial();

                double used = comp.getRequiredQuantity() * bestProductQuantity;
                double remaining = material.getStockQuantity() - used;

                materialsUsed.put(material.getName(), used);
                materialsRemaining.put(material.getName(), remaining);
            }

            recommendation = String.format(
                    "Recomendação: Produzir %s. Quantidade: %d unidades. Lucro total: R$ %.2f. Material limitante: %s",
                    bestProductName,
                    bestProductQuantity,
                    bestProductTotalProfit,
                    limitingMaterial
            );
        }

        return new OptimizationResponse(
                analyses,
                bestProductName,
                bestProductQuantity,
                bestProductTotalProfit,
                limitingMaterial,
                materialsUsed,
                materialsRemaining,
                recommendation
        );
    }

    /**
     * Verificar se produto é viável com estoque atual
     */
    public boolean isProductFeasible(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        for (ProductComposition comp : product.getComposition()) {
            if (comp.getRawMaterial().getStockQuantity() < comp.getRequiredQuantity()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Simular produção
     */
    public Map<String, Object> simulateProduction(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Verificar estoque
        Map<String, Double> required = new HashMap<>();
        Map<String, Double> available = new HashMap<>();

        for (ProductComposition comp : product.getComposition()) {
            String materialName = comp.getRawMaterial().getName();
            Double totalNeeded = comp.getRequiredQuantity() * quantity;
            Double stockAvailable = comp.getRawMaterial().getStockQuantity();

            required.put(materialName, totalNeeded);
            available.put(materialName, stockAvailable);

            if (stockAvailable < totalNeeded) {
                throw new RuntimeException("Estoque insuficiente de " + materialName);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("product", product.getName());
        result.put("quantity", quantity);
        result.put("totalProfit", product.getProfit() * quantity);
        result.put("materialsRequired", required);
        result.put("materialsAvailable", available);

        return result;
    }

    /**
     * Produzir (consumir matérias-primas)
     */
    @Transactional
    public void produce(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        for (ProductComposition comp : product.getComposition()) {
            RawMaterial material = comp.getRawMaterial();
            Double totalNeeded = comp.getRequiredQuantity() * quantity;

            if (material.getStockQuantity() < totalNeeded) {
                throw new RuntimeException("Estoque insuficiente de " + material.getName());
            }

            material.setStockQuantity(material.getStockQuantity() - totalNeeded);
            rawMaterialRepository.save(material);
        }
    }
}
