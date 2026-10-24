package cl.eventpass.bff.controller;

import cl.eventpass.bff.controller.docs.OrderControllerDocs;
import cl.eventpass.bff.dto.request.OrderCreateRequest;
import cl.eventpass.bff.dto.request.OrderStatusUpdateRequest;
import cl.eventpass.bff.dto.response.DashboardSummaryResponse;
import cl.eventpass.bff.dto.response.OrderResponse;
import cl.eventpass.bff.dto.response.StandardResponse;
import cl.eventpass.bff.enums.OrderStateEnum;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.util.UUID;

public class OrderController implements OrderControllerDocs {

    @Override
    public StandardResponse<OrderResponse> createOrder(OrderCreateRequest request) {
        return null;
    }

    @Override
    public StandardResponse<Page<OrderResponse>> findAll(UUID customerId, OrderStateEnum status, Pageable pageable) {
        return null;
    }

    @Override
    public StandardResponse<Page<OrderResponse>> findMyOrders(Pageable pageable) {
        return null;
    }

    @Override
    public StandardResponse<OrderResponse> findById(UUID orderId) {
        return null;
    }

    @Override
    public StandardResponse<OrderResponse> cancel(UUID orderId) {
        return null;
    }

    @Override
    public StandardResponse<OrderResponse> updateStatus(UUID orderId, OrderStatusUpdateRequest request) {
        return null;
    }

    @Override
    public StandardResponse<OrderResponse> accept(UUID orderId) {
        return null;
    }

    @Override
    public StandardResponse<DashboardSummaryResponse> getDashboardSummary() {
        return null;
    }
}
