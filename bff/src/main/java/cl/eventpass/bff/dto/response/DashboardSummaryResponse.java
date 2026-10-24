package cl.eventpass.bff.dto.response;

import java.util.Map;

/**
 * DTO de respuesta que representa el resumen del tablero (dashboard) de órdenes.
 *
 * <p>Expone información métrica consolidada sobre el estado de las órdenes en el sistema,
 * incluyendo el total general de pedidos registrados, la distribución de los mismos
 * según su estado y la cantidad de órdenes pendientes de aceptación.
 * </p>
 *
 * @param totalOrders total general de órdenes registradas en el sistema.
 * @param orderByStatus mapa que contiene la cantidad de órdenes agrupadas por cada estado.
 * @param pendingAcceptance cantidad de órdenes que se encuentran pendientes de ser aceptadas.
 */
public record DashboardSummaryResponse(

        long totalOrders,
        Map<String, Long> orderByStatus,
        long pendingAcceptance

) {
}
