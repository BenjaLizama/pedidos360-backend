package cl.pedidos360.ms_catalog.service.impl;

import cl.pedidos360.ms_catalog.dto.response.PriceHistoryResponse;
import cl.pedidos360.ms_catalog.entity.PriceHistoryEntity;
import cl.pedidos360.ms_catalog.mapper.PriceHistoryMapper;
import cl.pedidos360.ms_catalog.observability.CorrelationIdProvider;
import cl.pedidos360.ms_catalog.repository.PriceHistoryRepository;
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
class PriceHistoryServiceImplTest {

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private PriceHistoryMapper priceHistoryMapper;

    @Mock
    private CorrelationIdProvider correlationIdProvider;

    @InjectMocks
    private PriceHistoryServiceImpl priceHistoryService;

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
    void findByProductId_shouldReturnMappedHistoryPage() {
        PageRequest pageable = PageRequest.of(0, 10);

        PriceHistoryEntity history = mock(PriceHistoryEntity.class);
        PriceHistoryResponse expectedResponse =
                mock(PriceHistoryResponse.class);

        Page<PriceHistoryEntity> historyPage =
                new PageImpl<>(List.of(history), pageable, 1);

        when(priceHistoryRepository.findByProductId(productId, pageable))
                .thenReturn(historyPage);

        when(priceHistoryMapper.toResponse(history))
                .thenReturn(expectedResponse);

        Page<PriceHistoryResponse> response =
                priceHistoryService.findByProductId(productId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        assertSame(expectedResponse, response.getContent().get(0));

        verify(priceHistoryRepository)
                .findByProductId(productId, pageable);

        verify(priceHistoryMapper)
                .toResponse(history);
    }

    @Test
    void findByProductId_shouldReturnEmptyPageWhenNoHistoryExists() {
        PageRequest pageable = PageRequest.of(0, 10);

        Page<PriceHistoryEntity> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(priceHistoryRepository.findByProductId(productId, pageable))
                .thenReturn(emptyPage);

        Page<PriceHistoryResponse> response =
                priceHistoryService.findByProductId(productId, pageable);

        assertNotNull(response);
        assertTrue(response.isEmpty());
        assertEquals(0, response.getTotalElements());

        verify(priceHistoryRepository)
                .findByProductId(productId, pageable);

        verifyNoInteractions(priceHistoryMapper);
    }

    @Test
    void findByProductId_shouldPreservePaginationMetadata() {
        PageRequest pageable = PageRequest.of(1, 2);

        PriceHistoryEntity first = mock(PriceHistoryEntity.class);
        PriceHistoryEntity second = mock(PriceHistoryEntity.class);

        PriceHistoryResponse firstResponse =
                mock(PriceHistoryResponse.class);

        PriceHistoryResponse secondResponse =
                mock(PriceHistoryResponse.class);

        Page<PriceHistoryEntity> historyPage =
                new PageImpl<>(
                        List.of(first, second),
                        pageable,
                        5
                );

        when(priceHistoryRepository.findByProductId(productId, pageable))
                .thenReturn(historyPage);

        when(priceHistoryMapper.toResponse(first))
                .thenReturn(firstResponse);

        when(priceHistoryMapper.toResponse(second))
                .thenReturn(secondResponse);

        Page<PriceHistoryResponse> response =
                priceHistoryService.findByProductId(productId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getNumber());
        assertEquals(2, response.getSize());
        assertEquals(2, response.getNumberOfElements());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertSame(firstResponse, response.getContent().get(0));
        assertSame(secondResponse, response.getContent().get(1));

        verify(priceHistoryRepository)
                .findByProductId(productId, pageable);

        verify(priceHistoryMapper).toResponse(first);
        verify(priceHistoryMapper).toResponse(second);
    }
}
