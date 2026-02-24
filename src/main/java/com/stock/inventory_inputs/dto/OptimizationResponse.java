package com.stock.inventory_inputs.dto;

import java.util.List;
import java.util.Map;

// Resposta da otimização
public record OptimizationResponse(
        List<ProductAnalysis> productAnalyses,
        String bestProductName,
        Integer bestProductQuantity,
        Double bestProductTotalProfit,
        String limitingMaterial,
        Map<String, Double> materialsUsed,
        Map<String, Double> materialsRemaining,
        String recommendation
) {
}
