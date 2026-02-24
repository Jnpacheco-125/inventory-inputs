package com.stock.inventory_inputs.dto;

public record ProductCompositionDTO(
        Long rawMaterialId,
        String rawMaterialCode,
        String rawMaterialName,
        Double requiredQuantity,
        String unitOfMeasure
) {
}
