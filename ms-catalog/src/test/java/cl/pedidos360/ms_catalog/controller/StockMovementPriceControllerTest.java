package cl.pedidos360.ms_catalog.controller;

import cl.pedidos360.ms_catalog.dto.response.StockMovementResponse;
import cl.pedidos360.ms_catalog.enums.StockMovementType;
import cl.pedidos360.ms_catalog.service.StockMovementService;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StockMovementPriceControllerTest {

    private static final UUID PRODUCT_ID =
            UUID.fromString("a1234567-1234-4234-8234-123456789abc");

    @Mock
    private StockMovementService stockMovementService;

    @InjectMocks
    private StockMovementPriceController stockMovementPriceController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(stockMovementPriceController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void findByProductIdShouldReturnPagedStockMovements()
            throws Exception {

        StockMovementResponse movement = createStockMovementResponse();

        Page<StockMovementResponse> page = new PageImpl<>(
                List.of(movement),
                PageRequest.of(0, 20),
                1
        );

        when(stockMovementService.findByProductId(
                PRODUCT_ID,
                PageRequest.of(0, 20)
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/stock-movement/{productId}", PRODUCT_ID)
                                .param("page", "0")
                                .param("size", "20")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Histórico de movimiento de stock obtenido con exito."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].productId")
                        .value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.data.content[0].quantity").value(5))
                .andExpect(jsonPath("$.data.content[0].previousStock").value(10))
                .andExpect(jsonPath("$.data.content[0].resultingStock").value(15));

        verify(stockMovementService).findByProductId(
                PRODUCT_ID,
                PageRequest.of(0, 20)
        );
    }

    @Test
    void findByProductIdShouldReturnEmptyPageWhenNoMovementsExist()
            throws Exception {

        Page<StockMovementResponse> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );

        when(stockMovementService.findByProductId(
                PRODUCT_ID,
                PageRequest.of(0, 20)
        )).thenReturn(emptyPage);

        mockMvc.perform(
                        get("/api/v1/stock-movement/{productId}", PRODUCT_ID)
                                .param("page", "0")
                                .param("size", "20")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(0));

        verify(stockMovementService).findByProductId(
                PRODUCT_ID,
                PageRequest.of(0, 20)
        );
    }

    @Test
    void findByProductIdShouldRespectPaginationParameters()
            throws Exception {

        Page<StockMovementResponse> page = new PageImpl<>(
                List.of(),
                PageRequest.of(1, 5),
                0
        );

        when(stockMovementService.findByProductId(
                PRODUCT_ID,
                PageRequest.of(1, 5)
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/stock-movement/{productId}", PRODUCT_ID)
                                .param("page", "1")
                                .param("size", "5")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.number").value(1))
                .andExpect(jsonPath("$.data.size").value(5));

        verify(stockMovementService).findByProductId(
                PRODUCT_ID,
                PageRequest.of(1, 5)
        );
    }

    private StockMovementResponse createStockMovementResponse() {
        return new StockMovementResponse(
                UUID.fromString("b1234567-1234-4234-8234-123456789abc"),
                PRODUCT_ID,
                StockMovementType.RESTOCK,
                5,
                10,
                15,
                "Reposición de inventario",
                UUID.fromString("c1234567-1234-4234-8234-123456789abc"),
                Instant.parse("2026-10-01T10:00:00Z"),
                UUID.fromString("d1234567-1234-4234-8234-123456789abc")
        );
    }
}
