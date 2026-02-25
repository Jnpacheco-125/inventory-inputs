package com.stock.inventory_inputs.service;
import com.stock.inventory_inputs.dto.OptimizationResponse;
import com.stock.inventory_inputs.model.Product;
import com.stock.inventory_inputs.model.ProductComposition;
import com.stock.inventory_inputs.model.RawMaterial;
import com.stock.inventory_inputs.repository.ProductRepository;
import com.stock.inventory_inputs.repository.RawMaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OptimizationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @InjectMocks
    private OptimizationService optimizationService;

    private RawMaterial metal;
    private RawMaterial plastic;
    private RawMaterial paint;
    private Product chair;
    private Product table;
    private Product stool;

    @BeforeEach
    void setUp() {

        metal = new RawMaterial(1L, "001", "Metal", 300.0, "kg");
        plastic = new RawMaterial(2L, "002", "Plastic", 150.0, "kg");
        paint = new RawMaterial(3L, "003", "Paint", 35.0, "L");

        chair = new Product();
        chair.setId(1L);
        chair.setName("Chair");
        chair.setProfit(45.0);

        table = new Product();
        table.setId(2L);
        table.setName("Table");
        table.setProfit(70.0);

        stool = new Product();
        stool.setId(3L);
        stool.setName("Stool");
        stool.setProfit(22.0);

        addComposition(chair, metal, 5.0);
        addComposition(chair, plastic, 2.0);
        addComposition(chair, paint, 0.5);

        addComposition(table, metal, 10.0);
        addComposition(table, plastic, 3.0);
        addComposition(table, paint, 1.0);

        addComposition(stool, metal, 3.0);
        addComposition(stool, plastic, 1.0);
        addComposition(stool, paint, 0.3);

        lenient().when(productRepository.findById(anyLong()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    return List.of(chair, table, stool).stream()
                            .filter(p -> p.getId().equals(id))
                            .findFirst();
                });
    }

    private void addComposition(Product product, RawMaterial material, Double quantity) {
        ProductComposition comp = new ProductComposition();
        comp.setProduct(product);
        comp.setRawMaterial(material);
        comp.setRequiredQuantity(quantity);

        product.getComposition().add(comp);
    }

    @Test
    void shouldReturnProductsOrderedByProfit() {

        List<Product> products = List.of(chair, table, stool);

        when(productRepository.findAll()).thenReturn(products);
        when(rawMaterialRepository.findAll())
                .thenReturn(List.of(metal, plastic, paint));

        OptimizationResponse response = optimizationService.optimizeProduction();

        assertNotNull(response);
        assertEquals("Chair", response.productAnalyses().get(0).name());
    }

    @Test
    void shouldRespectTopNParameter() {


        List<Product> products = List.of(chair, table, stool);

        when(productRepository.findAll()).thenReturn(products);
        when(rawMaterialRepository.findAll())
                .thenReturn(List.of(metal, plastic, paint));

        OptimizationResponse response = optimizationService.optimizeProduction(2);

        assertNotNull(response);
        assertEquals(2, response.productAnalyses().size());
        assertEquals("Chair", response.productAnalyses().get(0).name());
        assertEquals("Stool", response.productAnalyses().get(1).name());
    }

    @Test
    void shouldReturnEmptyWhenNoProducts() {

        when(productRepository.findAll()).thenReturn(List.of());
        when(rawMaterialRepository.findAll())
                .thenReturn(List.of(metal, plastic, paint));

        OptimizationResponse response = optimizationService.optimizeProduction();

        assertTrue(response.productAnalyses().isEmpty());
        assertEquals(0, response.bestProductQuantity());
        assertEquals(0.0, response.bestProductTotalProfit());
    }

    @Test
    void shouldCalculateCorrectlyWithLimitedStock() {

        metal.setStockQuantity(300.0);

        when(productRepository.findAll())
                .thenReturn(List.of(chair, table, stool));

        when(rawMaterialRepository.findAll())
                .thenReturn(List.of(metal, plastic, paint));

        OptimizationResponse response = optimizationService.optimizeProduction();

        assertEquals(60, response.bestProductQuantity());
    }

    @Test
    void shouldCalculateMaterialsUsedCorrectly() {

        when(productRepository.findAll())
                .thenReturn(List.of(chair));

        when(rawMaterialRepository.findAll())
                .thenReturn(List.of(metal, plastic, paint));

        OptimizationResponse response = optimizationService.optimizeProduction();

        assertEquals(300.0, response.materialsUsed().get("Metal"), 0.01);
        assertEquals(120.0, response.materialsUsed().get("Plastic"), 0.01);
        assertEquals(30.0, response.materialsUsed().get("Paint"), 0.01);


        assertEquals(0.0, response.materialsRemaining().get("Metal"), 0.01);
        assertEquals(30.0, response.materialsRemaining().get("Plastic"), 0.01);
        assertEquals(5.0, response.materialsRemaining().get("Paint"), 0.01);
    }

    @Test
    void shouldReturnRecommendationWhenNothingCanBeProduced() {

        metal.setStockQuantity(0.0);

        when(productRepository.findAll()).thenReturn(List.of(chair));
        when(rawMaterialRepository.findAll())
                .thenReturn(List.of(metal, plastic, paint));

        OptimizationResponse response = optimizationService.optimizeProduction();

        assertEquals(
                "Não é possível produzir nenhum produto com o estoque atual.",
                response.recommendation()
        );
    }

    @Test
    void shouldHandleProductWithoutComposition() {

        Product emptyProduct = new Product();
        emptyProduct.setId(10L);
        emptyProduct.setName("Empty");
        emptyProduct.setProfit(10.0);

        when(productRepository.findAll()).thenReturn(List.of(emptyProduct));
        when(rawMaterialRepository.findAll()).thenReturn(List.of());

        OptimizationResponse response = optimizationService.optimizeProduction();

        assertEquals(0, response.bestProductQuantity());
    }

    @Test
    void shouldGenerateRecommendationWhenProductionIsPossible() {

        when(productRepository.findAll()).thenReturn(List.of(chair));
        when(rawMaterialRepository.findAll())
                .thenReturn(List.of(metal, plastic, paint));

        OptimizationResponse response = optimizationService.optimizeProduction();

        assertTrue(response.recommendation().contains("Recomendação: Produzir"));
        assertTrue(response.recommendation().contains("Quantidade:"));
    }
    @Test
    void shouldIgnoreMaterialWhenRequiredQuantityIsZero() {

        Product specialProduct = new Product();
        specialProduct.setId(99L);
        specialProduct.setName("Special");
        specialProduct.setProfit(100.0);

        addComposition(specialProduct, metal, 0.0);

        when(productRepository.findAll())
                .thenReturn(List.of(specialProduct));

        when(rawMaterialRepository.findAll())
                .thenReturn(List.of(metal));

        lenient().when(productRepository.findById(anyLong()))
                .thenReturn(Optional.of(specialProduct));

        OptimizationResponse response = optimizationService.optimizeProduction();

        assertEquals(0, response.bestProductQuantity());
        assertEquals(0.0, response.bestProductTotalProfit(), 0.01);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {

        when(productRepository.findAll())
                .thenReturn(List.of(chair));

        when(rawMaterialRepository.findAll())
                .thenReturn(List.of(metal, plastic, paint));

        when(productRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> optimizationService.optimizeProduction()
        );

        assertTrue(exception.getMessage().contains("Produto não encontrado"));
    }
}