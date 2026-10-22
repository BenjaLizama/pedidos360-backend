
package cl.pedidos360.ms_orders.controller;

import cl.pedidos360.ms_orders.controller.docs.OrderControllerDocs;
import cl.pedidos360.ms_orders.dto.request.OrderCreateRequest;
import cl.pedidos360.ms_orders.dto.request.OrderStatusUpdateRequest;
import cl.pedidos360.ms_orders.dto.response.OrderResponse;
import cl.pedidos360.ms_orders.dto.response.StandardResponse;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import cl.pedidos360.ms_orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador REST para la gestión de órdenes de compra.
 *
 * <p>Expone los endpoints para crear, consultar, cancelar y actualizar
 * órdenes. La documentación OpenAPI se encuentra centralizada en
 * {@link OrderControllerDocs}.</p>
 *
 * <p>Las respuestas exitosas utilizan {@link StandardResponse} para
 * mantener un formato uniforme en la API.</p>
 *
 * <p>Las validaciones de propiedad de las órdenes y de las transiciones
 * de estado corresponden a la capa de servicio.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {

    private final OrderService orderService;

    /**
     * Crea una orden para el cliente autenticado.
     *
     * @param request datos necesarios para crear la orden
     * @return respuesta HTTP 201 con la orden creada
     */
    @Override
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<StandardResponse<OrderResponse>> create(
            @Valid @RequestBody OrderCreateRequest request
    ) {
        log.info("Solicitud HTTP para crear una orden");

        OrderResponse response = orderService.create(request);

        return ResponseEntity
                .status(201)
                .body(StandardResponse.created(
                        "Orden creada con éxito.",
                        response
                ));
    }

    /**
     * Consulta todas las órdenes con filtros opcionales y paginación.
     * Disponible para operadores y administradores.
     *
     * @param customerId identificador opcional del cliente
     * @param status estado opcional de las órdenes
     * @param pageable parámetros de paginación y ordenamiento
     * @return respuesta HTTP 200 con la página de órdenes
     */
    @Override
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<StandardResponse<Page<OrderResponse>>> findAll(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStateEnum status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
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

        return ResponseEntity.ok(
                StandardResponse.ok(
                        "Órdenes consultadas con éxito.",
                        response
                )
        );
    }

    /**
     * Consulta exclusivamente las órdenes del cliente autenticado.
     *
     * @param pageable parámetros de paginación y ordenamiento
     * @return respuesta HTTP 200 con las órdenes del cliente
     */
    @Override
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<StandardResponse<Page<OrderResponse>>> findMyOrders(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<OrderResponse> response = orderService.findMyOrders(pageable);

        return ResponseEntity.ok(
                StandardResponse.ok(
                        "Tus órdenes fueron consultadas con éxito.",
                        response
                )
        );
    }

    /**
     * Consulta una orden por su identificador.
     *
     * <p>El servicio debe comprobar que el cliente autenticado sea
     * propietario de la orden cuando corresponda.</p>
     *
     * @param orderId identificador único de la orden
     * @return respuesta HTTP 200 con la orden encontrada
     */
    @Override
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'ADMIN')")
    public ResponseEntity<StandardResponse<OrderResponse>> findById(
            @PathVariable("orderId") UUID orderId
    ) {
        OrderResponse response = orderService.findById(orderId);

        return ResponseEntity.ok(
                StandardResponse.ok(
                        "Orden consultada con éxito.",
                        response
                )
        );
    }

    /**
     * Cancela una orden del cliente autenticado.
     *
     * <p>El servicio debe validar la propiedad de la orden y comprobar
     * que su estado permita la cancelación.</p>
     *
     * @param orderId identificador único de la orden
     * @return respuesta HTTP 200 con la orden actualizada
     */
    @Override
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<StandardResponse<OrderResponse>> cancel(
            @PathVariable("orderId") UUID orderId
    ) {
        log.info("Solicitud HTTP para cancelar orden | orderId={}", orderId);

        OrderResponse response = orderService.cancel(orderId);

        return ResponseEntity.ok(
                StandardResponse.ok(
                        "Orden cancelada con éxito.",
                        response
                )
        );
    }

    /**
     * Actualiza el estado de una orden.
     * Disponible para operadores y administradores.
     *
     * @param orderId identificador único de la orden
     * @param request solicitud que contiene el nuevo estado
     * @return respuesta HTTP 200 con la orden actualizada
     */
    @Override
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<StandardResponse<OrderResponse>> updateStatus(
            @PathVariable("orderId") UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderResponse response = orderService.updateStatus(
                orderId,
                request.status()
        );

        return ResponseEntity.ok(
                StandardResponse.ok(
                        "Estado de la orden actualizado con éxito.",
                        response
                )
        );
    }
}
