package com.stock.inventory_inputs.dto;

public record RawMaterialRequest(
        String code,
        String name,
        Double stockQuantity,
        String unitOfMeasure
) {
}
