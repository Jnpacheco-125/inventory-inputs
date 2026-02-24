package com.stock.inventory_inputs.controller;

import com.stock.inventory_inputs.dto.OptimizationResponse;
import com.stock.inventory_inputs.service.OptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production")
public class ProductionController {
    @Autowired
    private OptimizationService optimizationService;

    @GetMapping("/optimize")
    public ResponseEntity<OptimizationResponse> optimize() {
        OptimizationResponse response = optimizationService.optimizeProduction();
        return ResponseEntity.ok(response);
    }
}
