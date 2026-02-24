package com.stock.inventory_inputs.dto;

public record CompositionItem(
        String rawMaterialCode,
        Double requiredQuantity
) {
}
