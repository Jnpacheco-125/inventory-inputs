package com.stock.inventory_inputs.dto;
// Para criar matérias-primas via API
public record RawMaterialRequest(
        String code,
        String name,
        Double stockQuantity,
        String unitOfMeasure
) {
}
