package cl.pedidos360.ms_catalog.service.impl;

import cl.pedidos360.ms_catalog.dto.response.StockMovementResponse;
import cl.pedidos360.ms_catalog.entity.StockMovementEntity;
import cl.pedidos360.ms_catalog.mapper.StockMovementMapper;
import cl.pedidos360.ms_catalog.observability.CorrelationIdProvider;
import cl.pedidos360.ms_catalog.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceImplTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private StockMovementMapper stockMovementMapper;

    @Mock
    private CorrelationIdProvider correlationIdProvider;

    @InjectMocks
    private StockMovementServiceImpl stockMovementService;

    private UUID productId;
    private UUID correlationId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        correlationId = UUID.randomUUID();

        lenient()
                .when(correlationIdProvider.getCorrelationId())
                .thenReturn(correlationId);
    }

    @Test
    void findByProductId_shouldReturnMappedMovementPage() {
        PageRequest pageable = PageRequest.of(0, 10);

        StockMovementEntity movement =
                mock(StockMovementEntity.class);

        StockMovementResponse expectedResponse =
                mock(StockMovementResponse.class);

        Page<StockMovementEntity> movementPage =
                new PageImpl<>(List.of(movement), pageable, 1);

        when(stockMovementRepository.findByProductId(productId, pageable))
                .thenReturn(movementPage);

        when(stockMovementMapper.toResponse(movement))
                .thenReturn(expectedResponse);

        Page<StockMovementResponse> response =
                stockMovementService.findByProductId(productId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertSame(expectedResponse, response.getContent().get(0));

        verify(stockMovementRepository)
                .findByProductId(productId, pageable);

        verify(stockMovementMapper)
                .toResponse(movement);
    }

    @Test
    void findByProductId_shouldReturnEmptyPageWhenNoMovementsExist() {
        PageRequest pageable = PageRequest.of(0, 10);

        Page<StockMovementEntity> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(stockMovementRepository.findByProductId(productId, pageable))
                .thenReturn(emptyPage);

        Page<StockMovementResponse> response =
                stockMovementService.findByProductId(productId, pageable);

        assertNotNull(response);
        assertTrue(response.isEmpty());
        assertEquals(0, response.getTotalElements());

        verify(stockMovementRepository)
                .findByProductId(productId, pageable);

        verifyNoInteractions(stockMovementMapper);
    }

    @Test
    void findByProductId_shouldPreservePaginationMetadata() {
        PageRequest pageable = PageRequest.of(1, 2);

        StockMovementEntity first =
                mock(StockMovementEntity.class);

        StockMovementEntity second =
                mock(StockMovementEntity.class);

        StockMovementResponse firstResponse =
                mock(StockMovementResponse.class);

        StockMovementResponse secondResponse =
                mock(StockMovementResponse.class);

        Page<StockMovementEntity> movementPage =
                new PageImpl<>(
                        List.of(first, second),
                        pageable,
                        5
                );

        when(stockMovementRepository.findByProductId(productId, pageable))
                .thenReturn(movementPage);

        when(stockMovementMapper.toResponse(first))
                .thenReturn(firstResponse);

        when(stockMovementMapper.toResponse(second))
                .thenReturn(secondResponse);

        Page<StockMovementResponse> response =
                stockMovementService.findByProductId(productId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getNumber());
        assertEquals(2, response.getSize());
        assertEquals(2, response.getNumberOfElements());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertSame(firstResponse, response.getContent().get(0));
        assertSame(secondResponse, response.getContent().get(1));

        verify(stockMovementRepository)
                .findByProductId(productId, pageable);

        verify(stockMovementMapper).toResponse(first);
        verify(stockMovementMapper).toResponse(second);
    }
}
