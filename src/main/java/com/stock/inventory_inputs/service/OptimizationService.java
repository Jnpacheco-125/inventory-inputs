package com.stock.inventory_inputs.service;
import com.stock.inventory_inputs.dto.OptimizationResponse;
import com.stock.inventory_inputs.dto.ProductAnalysis;
import com.stock.inventory_inputs.model.Product;
import com.stock.inventory_inputs.model.ProductComposition;
import com.stock.inventory_inputs.model.RawMaterial;
import com.stock.inventory_inputs.repository.ProductRepository;
import com.stock.inventory_inputs.repository.RawMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OptimizationService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RawMaterialRepository rawMaterialRepository;

    /**
     * Método principal que faz toda a análise e otimização
     */
    public OptimizationResponse optimizeProduction(Integer topN) {
        // 1. Buscar todos os dados do banco
        List<Product> allProducts = productRepository.findAll();
        List<RawMaterial> allMaterials = rawMaterialRepository.findAll();

        // 2. Criar mapa de materiais para acesso fácil
        Map<String, RawMaterial> materialsByCode = allMaterials.stream()
                .collect(Collectors.toMap(RawMaterial::getCode, m -> m));

        // 3. Analisar cada produto individualmente
        List<ProductAnalysis> analyses = new ArrayList<>();

        for (Product product : allProducts) {
            analyses.add(analyzeProduct(product, materialsByCode));
        }

        // 4. Ordenar por maior lucro total possível
        analyses.sort((a, b) -> b.totalPossibleProfit().compareTo(a.totalPossibleProfit()));

        // 🔥 NOVO: Limitar para mostrar apenas os topN melhores produtos
        analyses = analyses.stream().limit(topN).collect(Collectors.toList());

        // 5. Pegar o melhor produto
        ProductAnalysis bestProduct = analyses.isEmpty() ? null : analyses.get(0);

        // 6. Gerar plano de produção para o melhor produto
        Map<String, Double> materialsUsed = new HashMap<>();
        Map<String, Double> materialsRemaining = new HashMap<>();

        if (bestProduct != null && bestProduct.maxPossibleUnits() > 0) {
            Product product = findProductById(bestProduct.id());
            Integer quantity = bestProduct.maxPossibleUnits();

            // Calcular materiais usados e restantes
            for (ProductComposition comp : product.getComposition()) {
                RawMaterial material = comp.getRawMaterial();
                Double totalNeeded = comp.getRequiredQuantity() * quantity;
                Double remaining = material.getStockQuantity() - totalNeeded;

                materialsUsed.put(material.getName(), totalNeeded);
                materialsRemaining.put(material.getName(), remaining);
            }
        }
        // 7. Criar recomendação em texto
        String recommendation = createRecommendation(bestProduct, allMaterials);

        // 8. Montar resposta
        return new OptimizationResponse(
                analyses,
                bestProduct != null ? bestProduct.name() : "Nenhum produto",
                bestProduct != null ? bestProduct.maxPossibleUnits() : 0,
                bestProduct != null ? bestProduct.totalPossibleProfit() : 0.0,
                bestProduct != null ? bestProduct.limitingMaterial() : "",
                materialsUsed,
                materialsRemaining,
                recommendation
        );
    }

    // 🔥 Sobrecarga para manter compatibilidade (quando não passar parâmetro)
    public OptimizationResponse optimizeProduction() {
        return optimizeProduction(3); // default = 3
    }

    /**
     * Analisa um produto específico
     */
    private ProductAnalysis analyzeProduct(Product product, Map<String, RawMaterial> materialsByCode) {
        Integer maxUnits = Integer.MAX_VALUE;
        String limitingMaterial = "";
        Map<String, Double> materialsRequired = new HashMap<>();

        // Para cada matéria-prima na composição do produto
        for (ProductComposition comp : product.getComposition()) {
            RawMaterial material = comp.getRawMaterial();
            Double requiredPerUnit = comp.getRequiredQuantity();
            Double available = material.getStockQuantity();

            // Guardar quanto precisa (para mostrar no resultado)
            materialsRequired.put(material.getName() + " (" + material.getUnitOfMeasure() + ")",
                    requiredPerUnit);

            // Calcular quantas unidades esse material permite produzir
            if (requiredPerUnit > 0) {
                Integer possibleUnits = (int) Math.floor(available / requiredPerUnit);
                if (possibleUnits < maxUnits) {
                    maxUnits = possibleUnits;
                    limitingMaterial = material.getName();
                }
            }
        }

        // Se não consegue produzir nenhuma
        if (maxUnits == Integer.MAX_VALUE) {
            maxUnits = 0;
        }

        Double totalProfit = product.getProfit() * maxUnits;

        return new ProductAnalysis(
                product.getId(),
                product.getName(),
                product.getProfit(),
                maxUnits,
                totalProfit,
                limitingMaterial,
                materialsRequired
        );
    }
    /**
     * Encontra um produto pelo ID
     */
    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id));
    }

    /**
     * Cria uma recomendação em texto
     */
    private String createRecommendation(ProductAnalysis bestProduct, List<RawMaterial> allMaterials) {
        if (bestProduct == null || bestProduct.maxPossibleUnits() == 0) {
            return "Não é possível produzir nenhum produto com o estoque atual.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Recomendação: Produzir %s%n", bestProduct.name()));
        sb.append(String.format("Quantidade: %d unidades%n", bestProduct.maxPossibleUnits()));
        sb.append(String.format("Lucro total: R$ %.2f%n", bestProduct.totalPossibleProfit()));
        sb.append(String.format("Material limitante: %s%n", bestProduct.limitingMaterial()));

        sb.append("\nMateriais necessários por unidade:\n");
        for (Map.Entry<String, Double> entry : bestProduct.materialsRequired().entrySet()) {
            sb.append(String.format("- %s: %.2f%n", entry.getKey(), entry.getValue()));
        }

        return sb.toString();
    }
}
