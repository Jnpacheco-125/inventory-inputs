package com.stock.inventory_inputs.service;

import com.stock.inventory_inputs.dto.RawMaterialRequest;
import com.stock.inventory_inputs.dto.RawMaterialResponseDTO;
import com.stock.inventory_inputs.model.RawMaterial;
import com.stock.inventory_inputs.repository.ProductCompositionRepository;
import com.stock.inventory_inputs.repository.RawMaterialRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RawMaterialService {
    @Autowired
    private RawMaterialRepository rawMaterialRepository;

    @Autowired
    private ProductCompositionRepository productCompositionRepository;

    private RawMaterial toEntity(RawMaterialRequest request) {
        RawMaterial material = new RawMaterial();
        material.setCode(request.code());
        material.setName(request.name());
        material.setStockQuantity(request.stockQuantity());
        material.setUnitOfMeasure(request.unitOfMeasure());
        return material;
    }

    private RawMaterialRequest toDTO(RawMaterial material) {
        return new RawMaterialRequest(
                material.getCode(),
                material.getName(),
                material.getStockQuantity(),
                material.getUnitOfMeasure()
        );
    }
    private RawMaterialResponseDTO toResponseDTO(RawMaterial material) {
        return new RawMaterialResponseDTO(
                material.getId(),
                material.getCode(),
                material.getName(),
                material.getStockQuantity(),
                material.getUnitOfMeasure()
        );
    }

    public List<RawMaterialResponseDTO> findAll() {
        return rawMaterialRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public RawMaterialResponseDTO findById(Long id) {
        return rawMaterialRepository.findById(id)
                .map(this::toResponseDTO)
                .orElse(null);
    }

    public RawMaterialRequest findByCode(String code) {
        return rawMaterialRepository.findByCode(code)
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional
    public RawMaterialRequest create(RawMaterialRequest request) {
        if (rawMaterialRepository.findByCode(request.code()).isPresent()) {
            throw new RuntimeException("Já existe uma matéria-prima com o código: " + request.code());
        }

        if (request.stockQuantity() < 0) {
            throw new RuntimeException("A quantidade em estoque não pode ser negativa");
        }

        RawMaterial material = toEntity(request);
        RawMaterial saved = rawMaterialRepository.save(material);
        return toDTO(saved);
    }

    @Transactional
    public RawMaterialResponseDTO update(Long id, RawMaterialRequest request) {
        RawMaterial existing = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matéria-prima não encontrada com ID: " + id));

        if (!existing.getCode().equals(request.code())) {
            if (rawMaterialRepository.findByCode(request.code()).isPresent()) {
                throw new RuntimeException("Já existe uma matéria-prima com o código: " + request.code());
            }
        }

        if (request.stockQuantity() < 0) {
            throw new RuntimeException("A quantidade em estoque não pode ser negativa");
        }

        existing.setCode(request.code());
        existing.setName(request.name());
        existing.setStockQuantity(request.stockQuantity());
        existing.setUnitOfMeasure(request.unitOfMeasure());

        RawMaterial updated = rawMaterialRepository.save(existing);
        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(Long id) {

        if (productCompositionRepository.existsByRawMaterialId(id)) {
            throw new RuntimeException(
                    "Não é possível deletar: matéria-prima está sendo usada em produtos"
            );
        }

        RawMaterial material = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Matéria-prima não encontrada com ID: " + id
                ));

        rawMaterialRepository.delete(material);
    }

    @Transactional
    public RawMaterialResponseDTO addToStock(Long id, Double quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantidade deve ser positiva");
        }

        RawMaterial material = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matéria-prima não encontrada"));

        material.setStockQuantity(material.getStockQuantity() + quantity);
        RawMaterial updated = rawMaterialRepository.save(material);
        return toResponseDTO(updated);
    }

    @Transactional
    public RawMaterialResponseDTO removeFromStock(Long id, Double quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantidade deve ser positiva");
        }

        RawMaterial material = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matéria-prima não encontrada"));

        if (material.getStockQuantity() < quantity) {
            throw new RuntimeException("Estoque insuficiente. Disponível: " + material.getStockQuantity());
        }

        material.setStockQuantity(material.getStockQuantity() - quantity);
        RawMaterial updated = rawMaterialRepository.save(material);
        return toResponseDTO(updated);
    }
}
