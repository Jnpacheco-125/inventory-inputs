package com.stock.inventory_inputs.dto;

import java.util.List;

// Para criar produtos via API
public record ProductRequest(
        String code,
        String name,
        Double price,
        Double profit,
        List<CompositionItem> composition
) {
}
