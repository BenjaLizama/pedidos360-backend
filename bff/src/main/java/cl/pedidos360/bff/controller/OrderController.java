package cl.pedidos360.bff.controller;

import cl.pedidos360.bff.client.OrdersClient;
import cl.pedidos360.bff.controller.docs.OrderControllerDocs;
import cl.pedidos360.bff.dto.request.OrderCreateRequest;
import cl.pedidos360.bff.dto.request.OrderStatusUpdateRequest;
import cl.pedidos360.bff.dto.response.DashboardSummaryResponse;
import cl.pedidos360.bff.dto.response.OrderResponse;
import cl.pedidos360.bff.dto.response.StandardResponse;
import cl.pedidos360.bff.enums.OrderStateEnum;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {

    private final OrdersClient ordersClient;

    /**
     * Crea un pedido para el usuario autenticado.
     *
     * La creación y la publicación del evento son responsabilidad
     * de ms-orders.
     *
     * @param request datos necesarios para crear el pedido
     * @return respuesta con el pedido creado
     */
    @Override
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    public StandardResponse<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request
    ) {
        return ordersClient.createOrder(request);
    }

    /**
     * Obtiene pedidos con filtros opcionales de cliente y estado.
     */
    @Override
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<Page<OrderResponse>> findAll(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStateEnum status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ordersClient.findAll(customerId, status, pageable);
    }

    /**
     * Obtiene los pedidos del usuario autenticado.
     */
    @Override
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<Page<OrderResponse>> findMyOrders(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ordersClient.findMyOrders(pageable);
    }

    /**
     * Busca un pedido por su identificador.
     */
    @Override
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<OrderResponse> findById(
            @PathVariable UUID orderId
    ) {
        return ordersClient.findById(orderId);
    }

    /**
     * Cancela un pedido.
     */
    @Override
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<OrderResponse> cancel(
            @PathVariable UUID orderId
    ) {
        return ordersClient.cancel(orderId);
    }

    /**
     * Actualiza el estado de un pedido.
     */
    @Override
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<OrderResponse> updateStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return ordersClient.updateStatus(orderId, request);
    }

    /**
     * Acepta un pedido.
     */
    @Override
    @PatchMapping("/{orderId}/accept")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<OrderResponse> accept(
            @PathVariable UUID orderId
    ) {
        return ordersClient.accept(orderId);
    }

    /**
     * Obtiene el resumen del dashboard de pedidos.
     */
    @Override
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<DashboardSummaryResponse> getDashboardSummary() {
        return ordersClient.getDashboardSummary();
    }
}
