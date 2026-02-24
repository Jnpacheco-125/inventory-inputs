package com.stock.inventory_inputs.dto;

import java.util.List;

public record ProductResponseDTO(
        Long id,
        String code,
        String name,
        Double price,
        Double profit,
        List<ProductCompositionDTO> composition
) {
}
