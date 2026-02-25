package com.stock.inventory_inputs.dto;

import java.util.Map;

public record ProductAnalysis(
        Long id,
        String name,
        Double profitPerUnit,
        Integer maxPossibleUnits,
        Double totalPossibleProfit,
        String limitingMaterial,
        Map<String, Double> materialsRequired
) {
}
