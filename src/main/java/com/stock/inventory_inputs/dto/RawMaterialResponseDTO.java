package com.stock.inventory_inputs.dto;

public record RawMaterialResponseDTO(
        Long id,              // ✅ ID incluído
        String code,
        String name,
        Double stockQuantity,
        String unitOfMeasure
) {
}
