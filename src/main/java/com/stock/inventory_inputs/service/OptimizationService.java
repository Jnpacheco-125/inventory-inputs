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

    public OptimizationResponse optimizeProduction(Integer topN) {
        List<Product> allProducts = productRepository.findAll();
        List<RawMaterial> allMaterials = rawMaterialRepository.findAll();

        Map<String, RawMaterial> materialsByCode = allMaterials.stream()
                .collect(Collectors.toMap(RawMaterial::getCode, m -> m));

        List<ProductAnalysis> analyses = new ArrayList<>();

        for (Product product : allProducts) {
            analyses.add(analyzeProduct(product, materialsByCode));
        }

        analyses.sort((a, b) -> b.totalPossibleProfit().compareTo(a.totalPossibleProfit()));

        analyses = analyses.stream().limit(topN).collect(Collectors.toList());

        ProductAnalysis bestProduct = analyses.isEmpty() ? null : analyses.get(0);

        Map<String, Double> materialsUsed = new HashMap<>();
        Map<String, Double> materialsRemaining = new HashMap<>();

        if (bestProduct != null && bestProduct.maxPossibleUnits() > 0) {
            Product product = findProductById(bestProduct.id());
            Integer quantity = bestProduct.maxPossibleUnits();

            for (ProductComposition comp : product.getComposition()) {
                RawMaterial material = comp.getRawMaterial();
                Double totalNeeded = comp.getRequiredQuantity() * quantity;
                Double remaining = material.getStockQuantity() - totalNeeded;

                materialsUsed.put(material.getName(), totalNeeded);
                materialsRemaining.put(material.getName(), remaining);
            }
        }

        String recommendation = createRecommendation(bestProduct, allMaterials);

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

    public OptimizationResponse optimizeProduction() {
        return optimizeProduction(3); // default = 3
    }

    private ProductAnalysis analyzeProduct(Product product, Map<String, RawMaterial> materialsByCode) {
        Integer maxUnits = Integer.MAX_VALUE;
        String limitingMaterial = "";
        Map<String, Double> materialsRequired = new HashMap<>();

        for (ProductComposition comp : product.getComposition()) {
            RawMaterial material = comp.getRawMaterial();
            Double requiredPerUnit = comp.getRequiredQuantity();
            Double available = material.getStockQuantity();

            materialsRequired.put(material.getName() + " (" + material.getUnitOfMeasure() + ")",
                    requiredPerUnit);

            if (requiredPerUnit > 0) {
                Integer possibleUnits = (int) Math.floor(available / requiredPerUnit);
                if (possibleUnits < maxUnits) {
                    maxUnits = possibleUnits;
                    limitingMaterial = material.getName();
                }
            }
        }

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

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id));
    }

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
