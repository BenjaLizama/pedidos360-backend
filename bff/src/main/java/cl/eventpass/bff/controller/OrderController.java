package cl.eventpass.bff.controller;

import cl.eventpass.bff.client.OrdersClient;
import cl.eventpass.bff.controller.docs.OrderControllerDocs;
import cl.eventpass.bff.dto.request.OrderCreateRequest;
import cl.eventpass.bff.dto.request.OrderStatusUpdateRequest;
import cl.eventpass.bff.dto.response.DashboardSummaryResponse;
import cl.eventpass.bff.dto.response.OrderResponse;
import cl.eventpass.bff.dto.response.StandardResponse;
import cl.eventpass.bff.enums.OrderStateEnum;

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
     *
     * @param customerId identificador del cliente
     * @param status estado del pedido
     * @param pageable parámetros de paginación y ordenamiento
     * @return página de pedidos
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
     *
     * @param pageable parámetros de paginación y ordenamiento
     * @return página de pedidos del usuario actual
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
     *
     * @param orderId identificador del pedido
     * @return pedido encontrado
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
     *
     * @param orderId identificador del pedido
     * @return pedido actualizado
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
     *
     * @param orderId identificador del pedido
     * @param request nuevo estado solicitado
     * @return pedido actualizado
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
     *
     * @param orderId identificador del pedido
     * @return pedido aceptado
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
     *
     * @return resumen de pedidos
     */
    @Override
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    @ResponseStatus(HttpStatus.OK)
    public StandardResponse<DashboardSummaryResponse> getDashboardSummary() {
        return ordersClient.getDashboardSummary();
    }
}
