package cl.pedidos360.ms_orders.controller;

import cl.pedidos360.ms_orders.dto.request.OrderCreateRequest;
import cl.pedidos360.ms_orders.dto.request.OrderStatusUpdateRequest;
import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import cl.pedidos360.ms_orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Crea una orden para el cliente autenticado.
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody OrderCreateRequest request
    ) {
        log.info("Solicitud HTTP para crear una orden");

        OrderResponse response = orderService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Lista todas las órdenes, con filtros opcionales.
     * Disponible para operadores y administradores.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Page<OrderResponse>> findAll(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStateEnum status,
            @PageableDefault(size = 10, sort = "createdAt")
            Pageable pageable
    ) {
        log.debug(
                "Solicitud HTTP para listar órdenes | customerId={} | status={}",
                customerId,
                status
        );

        Page<OrderResponse> response = orderService.findAll(
                customerId,
                status,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Lista únicamente las órdenes del cliente autenticado.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Page<OrderResponse>> findMyOrders(
            @PageableDefault(size = 10, sort = "createdAt")
            Pageable pageable
    ) {
        Page<OrderResponse> response = orderService.findMyOrders(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene una orden.
     * Los clientes solo pueden consultar sus propias órdenes.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'ADMIN')")
    public ResponseEntity<OrderResponse> findById(
            @PathVariable UUID orderId
    ) {
        OrderResponse response = orderService.findById(orderId);

        return ResponseEntity.ok(response);
    }

    /**
     * Cancela una orden propia que todavía esté en estado CREADO.
     */
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<OrderResponse> cancel(
            @PathVariable UUID orderId
    ) {
        log.info("Solicitud HTTP para cancelar orden | orderId={}", orderId);

        OrderResponse response = orderService.cancel(orderId);

        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza el estado de una orden.
     * Disponible para operadores y administradores.
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderResponse response = orderService.updateStatus(
                orderId,
                request.status()
        );

        return ResponseEntity.ok(response);
    }
}
