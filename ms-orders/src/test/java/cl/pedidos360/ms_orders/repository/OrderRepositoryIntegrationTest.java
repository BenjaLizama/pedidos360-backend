package cl.pedidos360.ms_orders.repository;

import cl.pedidos360.ms_orders.config.JpaAuditingConfig;
import cl.pedidos360.ms_orders.entity.OrderEntity;
import cl.pedidos360.ms_orders.entity.OrderItemEntity;
import cl.pedidos360.ms_orders.enums.OrderStateEnum;
import cl.pedidos360.ms_orders.security.CurrentUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Testcontainers
@Import({
        JpaAuditingConfig.class,
        CurrentUserProvider.class,
        AuditingTestConfig.class
})
class OrderRepositoryIntegrationTest {

    private static final UUID USER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID OTHER_USER_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add(
                "spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver"
        );
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUpSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        USER_ID.toString(),
                        "test",
                        List.of()
                )
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSaveAndFindOrderById() {
        OrderEntity order = createOrder(USER_ID, firstState());

        OrderEntity saved = orderRepository.saveAndFlush(order);

        assertThat(saved.getId()).isNotNull();

        OrderEntity found = orderRepository.findById(saved.getId())
                .orElseThrow();

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getClientId()).isEqualTo(USER_ID);
        assertThat(found.getOrderState()).isEqualTo(firstState());
        assertThat(found.getTotal()).isEqualByComparingTo("10000.00");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getCreatedBy()).isEqualTo(USER_ID);
    }

    @Test
    void shouldFindOrdersByClientWithPagination() {
        orderRepository.saveAndFlush(createOrder(USER_ID, firstState()));
        orderRepository.saveAndFlush(createOrder(USER_ID, firstState()));
        orderRepository.saveAndFlush(createOrder(OTHER_USER_ID, firstState()));

        Page<OrderEntity> result = orderRepository.findAllByClientId(
                USER_ID,
                PageRequest.of(0, 1)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent())
                .allSatisfy(order ->
                        assertThat(order.getClientId()).isEqualTo(USER_ID)
                );
    }

    @Test
    void shouldFindOrderByIdAndClientId() {
        OrderEntity saved = orderRepository.saveAndFlush(
                createOrder(USER_ID, firstState())
        );

        assertThat(
                orderRepository.findByIdAndClientId(
                        saved.getId(),
                        USER_ID
                )
        ).isPresent();

        assertThat(
                orderRepository.findByIdAndClientId(
                        saved.getId(),
                        OTHER_USER_ID
                )
        ).isEmpty();
    }

    @Test
    void shouldFilterOrdersByClientAndState() {
        OrderStateEnum stateA = firstState();
        OrderStateEnum stateB = anotherState();

        orderRepository.saveAndFlush(createOrder(USER_ID, stateA));
        orderRepository.saveAndFlush(createOrder(USER_ID, stateB));
        orderRepository.saveAndFlush(createOrder(OTHER_USER_ID, stateA));

        Page<OrderEntity> result =
                orderRepository.findAllByClientIdAndOrderState(
                        USER_ID,
                        stateA,
                        PageRequest.of(0, 10)
                );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .allSatisfy(order -> {
                    assertThat(order.getClientId()).isEqualTo(USER_ID);
                    assertThat(order.getOrderState()).isEqualTo(stateA);
                });
    }

    @Test
    void shouldFilterAllOrdersByState() {
        OrderStateEnum stateA = firstState();
        OrderStateEnum stateB = anotherState();

        orderRepository.saveAndFlush(createOrder(USER_ID, stateA));
        orderRepository.saveAndFlush(createOrder(OTHER_USER_ID, stateA));
        orderRepository.saveAndFlush(createOrder(USER_ID, stateB));

        Page<OrderEntity> result = orderRepository.findAllByOrderState(
                stateA,
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .allSatisfy(order ->
                        assertThat(order.getOrderState()).isEqualTo(stateA)
                );
    }

    @Test
    void shouldFindItemsByOrderId() {
        OrderEntity order = createOrder(USER_ID, firstState());

        order.addItem(createItem(UUID.randomUUID(), 2, "5000.00"));
        order.addItem(createItem(UUID.randomUUID(), 1, "10000.00"));

        OrderEntity saved = orderRepository.saveAndFlush(order);

        List<OrderItemEntity> items =
                orderItemRepository.findAllByOrder_Id(saved.getId());

        assertThat(items).hasSize(2);
        assertThat(items)
                .allSatisfy(item ->
                        assertThat(item.getOrder().getId())
                                .isEqualTo(saved.getId())
                );
    }

    @Test
    void shouldFindItemsByProductId() {
        UUID productId = UUID.randomUUID();

        OrderEntity firstOrder = createOrder(USER_ID, firstState());
        firstOrder.addItem(createItem(productId, 2, "5000.00"));

        OrderEntity secondOrder = createOrder(OTHER_USER_ID, firstState());
        secondOrder.addItem(createItem(productId, 1, "5000.00"));

        OrderEntity thirdOrder = createOrder(USER_ID, firstState());
        thirdOrder.addItem(
                createItem(UUID.randomUUID(), 1, "3000.00")
        );

        orderRepository.saveAndFlush(firstOrder);
        orderRepository.saveAndFlush(secondOrder);
        orderRepository.saveAndFlush(thirdOrder);

        List<OrderItemEntity> items =
                orderItemRepository.findAllByProductId(productId);

        assertThat(items).hasSize(2);
        assertThat(items)
                .allSatisfy(item ->
                        assertThat(item.getProductId()).isEqualTo(productId)
                );
    }

    private OrderEntity createOrder(
            UUID clientId,
            OrderStateEnum state
    ) {
        return OrderEntity.builder()
                .clientId(clientId)
                .orderState(state)
                .total(new BigDecimal("10000.00"))
                .build();
    }

    private OrderItemEntity createItem(
            UUID productId,
            int quantity,
            String unitPrice
    ) {
        return OrderItemEntity.builder()
                .productId(productId)
                .quantity(quantity)
                .unitPrice(new BigDecimal(unitPrice))
                .build();
    }

    private OrderStateEnum firstState() {
        return OrderStateEnum.values()[0];
    }

    private OrderStateEnum anotherState() {
        return OrderStateEnum.values()[1];
    }
}
