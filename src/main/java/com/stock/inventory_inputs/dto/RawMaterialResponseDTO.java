package com.stock.inventory_inputs.dto;

public record RawMaterialResponseDTO(
        Long id,
        String code,
        String name,
        Double stockQuantity,
        String unitOfMeasure
) {
}
