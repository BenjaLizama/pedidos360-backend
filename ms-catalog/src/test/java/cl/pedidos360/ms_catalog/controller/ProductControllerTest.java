package cl.pedidos360.ms_catalog.controller;

import cl.pedidos360.ms_catalog.dto.request.*;
import cl.pedidos360.ms_catalog.dto.response.ProductResponse;
import cl.pedidos360.ms_catalog.enums.ProductStatus;
import cl.pedidos360.ms_catalog.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private static final UUID PRODUCT_ID =
            UUID.fromString("a1234567-1234-4234-8234-123456789abc");

    private static final UUID CATEGORY_ID =
            UUID.fromString("b1234567-1234-4234-8234-123456789abc");

    private static final Instant CREATED_AT =
            Instant.parse("2026-10-01T10:00:00Z");

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .setValidator(validator)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (validator != null) {
            validator.close();
        }
    }

    @Test
    void createShouldReturnCreatedWhenRequestIsValid()
            throws Exception {

        when(productService.create(org.mockito.ArgumentMatchers.any(
                ProductCreateRequest.class
        ))).thenReturn(createProductResponse());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "SKU-001",
                                  "name": "Teclado mecánico",
                                  "description": "Teclado de prueba",
                                  "price": 19990.00,
                                  "stock": 10,
                                  "categoryId": "%s"
                                }
                                """.formatted(CATEGORY_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message")
                        .value("Producto creado con éxito."))
                .andExpect(jsonPath("$.data.name")
                        .value("Teclado mecánico"))
                .andExpect(jsonPath("$.data.sku").value("SKU-001"));

        verify(productService).create(org.mockito.ArgumentMatchers.any(
                ProductCreateRequest.class
        ));
    }

    @Test
    void createShouldReturnBadRequestWhenNameIsBlank()
            throws Exception {

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "SKU-001",
                                  "name": " ",
                                  "description": "Descripción",
                                  "price": 19990.00,
                                  "stock": 10,
                                  "categoryId": "%s"
                                }
                                """.formatted(CATEGORY_ID)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByIdShouldReturnProduct()
            throws Exception {

        when(productService.findById(PRODUCT_ID))
                .thenReturn(createProductResponse());

        mockMvc.perform(get("/api/v1/products/{productId}", PRODUCT_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Producto recuperado con éxito."))
                .andExpect(jsonPath("$.data.id")
                        .value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.data.name")
                        .value("Teclado mecánico"));

        verify(productService).findById(PRODUCT_ID);
    }

    @Test
    void searchShouldReturnPagedProducts()
            throws Exception {

        Page<ProductResponse> page = new PageImpl<>(
                List.of(createProductResponse()),
                PageRequest.of(0, 20),
                1
        );

        when(productService.search(
                "Teclado",
                CATEGORY_ID,
                ProductStatus.ACTIVE,
                PageRequest.of(0, 20)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/products/search")
                        .param("name", "Teclado")
                        .param("categoryId", CATEGORY_ID.toString())
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Productos recuperados con éxito."))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name")
                        .value("Teclado mecánico"));

        verify(productService).search(
                "Teclado",
                CATEGORY_ID,
                ProductStatus.ACTIVE,
                PageRequest.of(0, 20)
        );
    }

    @Test
    void searchShouldAllowMissingFilters()
            throws Exception {

        Page<ProductResponse> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );

        when(productService.search(
                null,
                null,
                null,
                PageRequest.of(0, 20)
        )).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/products/search")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(0));

        verify(productService).search(
                null,
                null,
                null,
                PageRequest.of(0, 20)
        );
    }

    @Test
    void updateShouldReturnUpdatedProduct()
            throws Exception {

        when(productService.update(
                org.mockito.ArgumentMatchers.eq(PRODUCT_ID),
                org.mockito.ArgumentMatchers.any(ProductUpdateRequest.class)
        )).thenReturn(createProductResponse());

        mockMvc.perform(put("/api/v1/products/{productId}", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Teclado actualizado",
                                  "description": "Nueva descripción",
                                  "categoryId": "%s"
                                }
                                """.formatted(CATEGORY_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Producto actualizado con éxito."))
                .andExpect(jsonPath("$.data.id")
                        .value(PRODUCT_ID.toString()));

        verify(productService).update(
                org.mockito.ArgumentMatchers.eq(PRODUCT_ID),
                org.mockito.ArgumentMatchers.any(ProductUpdateRequest.class)
        );
    }

    @Test
    void updateShouldReturnBadRequestWhenNameIsBlank()
            throws Exception {

        mockMvc.perform(put("/api/v1/products/{productId}", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "description": "Descripción",
                                  "categoryId": "%s"
                                }
                                """.formatted(CATEGORY_ID)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatusShouldReturnUpdatedProduct()
            throws Exception {

        when(productService.updateStatus(
                org.mockito.ArgumentMatchers.eq(PRODUCT_ID),
                org.mockito.ArgumentMatchers.any(
                        ProductStatusUpdateRequest.class
                )
        )).thenReturn(createProductResponse());

        mockMvc.perform(patch(
                        "/api/v1/products/{productId}/status",
                        PRODUCT_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Estado del producto actualizado con éxito."))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(productService).updateStatus(
                org.mockito.ArgumentMatchers.eq(PRODUCT_ID),
                org.mockito.ArgumentMatchers.any(
                        ProductStatusUpdateRequest.class
                )
        );
    }

    @Test
    void updateStatusShouldReturnBadRequestWhenStatusIsNull()
            throws Exception {

        mockMvc.perform(patch(
                        "/api/v1/products/{productId}/status",
                        PRODUCT_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": null
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePriceShouldReturnUpdatedProduct()
            throws Exception {

        when(productService.updatePrice(
                org.mockito.ArgumentMatchers.eq(PRODUCT_ID),
                org.mockito.ArgumentMatchers.any(
                        ProductPriceUpdateRequest.class
                )
        )).thenReturn(createProductResponse());

        mockMvc.perform(patch(
                        "/api/v1/products/{productId}/price",
                        PRODUCT_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "price": 24990.00,
                                  "reason": "Ajuste comercial"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Precio del producto actualizado con éxito."))
                .andExpect(jsonPath("$.data.price").value(19990.00));

        verify(productService).updatePrice(
                org.mockito.ArgumentMatchers.eq(PRODUCT_ID),
                org.mockito.ArgumentMatchers.any(
                        ProductPriceUpdateRequest.class
                )
        );
    }

    @Test
    void updatePriceShouldReturnBadRequestWhenPriceIsNotPositive()
            throws Exception {

        mockMvc.perform(patch(
                        "/api/v1/products/{productId}/price",
                        PRODUCT_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "price": 0,
                                  "reason": "Precio inválido"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStockShouldReturnUpdatedProduct()
            throws Exception {

        when(productService.updateStock(
                org.mockito.ArgumentMatchers.eq(PRODUCT_ID),
                org.mockito.ArgumentMatchers.any(
                        ProductStockUpdateRequest.class
                )
        )).thenReturn(createProductResponse());

        mockMvc.perform(patch(
                        "/api/v1/products/{productId}/stock",
                        PRODUCT_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 3,
                                  "operation": "DECREASE",
                                  "reason": "Ajuste de inventario"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Stock del producto actualizado con éxito."))
                .andExpect(jsonPath("$.data.stock").value(10));

        verify(productService).updateStock(
                org.mockito.ArgumentMatchers.eq(PRODUCT_ID),
                org.mockito.ArgumentMatchers.any(
                        ProductStockUpdateRequest.class
                )
        );
    }

    @Test
    void updateStockShouldReturnBadRequestWhenQuantityIsNotPositive()
            throws Exception {

        mockMvc.perform(patch(
                        "/api/v1/products/{productId}/stock",
                        PRODUCT_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 0,
                                  "operation": "INCREASE",
                                  "reason": "Ajuste inválido"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void restockShouldReturnUpdatedProduct()
            throws Exception {

        when(productService.restock(
                org.mockito.ArgumentMatchers.eq(PRODUCT_ID),
                org.mockito.ArgumentMatchers.any(ProductRestockRequest.class)
        )).thenReturn(createProductResponse());

        mockMvc.perform(patch(
                        "/api/v1/products/{productId}/restock",
                        PRODUCT_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 5,
                                  "reason": "Reposición de inventario"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Stock del producto incrementado con éxito."))
                .andExpect(jsonPath("$.data.stock").value(10));

        verify(productService).restock(
                org.mockito.ArgumentMatchers.eq(PRODUCT_ID),
                org.mockito.ArgumentMatchers.any(ProductRestockRequest.class)
        );
    }

    @Test
    void restockShouldReturnBadRequestWhenReasonIsBlank()
            throws Exception {

        mockMvc.perform(patch(
                        "/api/v1/products/{productId}/restock",
                        PRODUCT_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 5,
                                  "reason": " "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteShouldReturnOkWhenProductIsDeleted()
            throws Exception {

        mockMvc.perform(delete(
                        "/api/v1/products/{productId}",
                        PRODUCT_ID
                )
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Producto eliminado con éxito."));

        verify(productService).delete(PRODUCT_ID);
    }

    private ProductResponse createProductResponse() {
        return new ProductResponse(
                PRODUCT_ID,
                "SKU-001",
                "Teclado mecánico",
                "Teclado de prueba",
                new BigDecimal("19990.00"),
                10,
                CATEGORY_ID,
                ProductStatus.ACTIVE,
                CREATED_AT,
                CREATED_AT
        );
    }
}
