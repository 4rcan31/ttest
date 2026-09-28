package com.sportshop.order.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.sportshop.common.security.AuthenticatedUser;
import com.sportshop.common.web.ApiException;
import com.sportshop.order.client.CatalogClient;
import com.sportshop.order.client.ReservedProduct;
import com.sportshop.order.client.StockItem;
import com.sportshop.order.domain.CartItem;
import com.sportshop.order.domain.Order;
import com.sportshop.order.domain.OrderItem;
import com.sportshop.order.domain.OrderStatus;
import com.sportshop.order.dto.OrderDtos.CreateOrderRequest;
import com.sportshop.order.dto.OrderDtos.OrderResponse;
import com.sportshop.order.dto.OrderDtos.OrderSummaryResponse;
import com.sportshop.order.dto.PageResponse;
import com.sportshop.order.repository.CartItemRepository;
import com.sportshop.order.repository.OrderRepository;

/**
 * Confirmación y seguimiento de órdenes.
 *
 * <p>La confirmación sigue un patrón Saga orquestado: (1) se reserva inventario en catalog-service,
 * (2) se persiste la orden y se vacía el carrito en una transacción local y (3) si el paso 2 falla,
 * se ejecuta la compensación liberando el inventario reservado.</p>
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final int MAX_ORDER_NUMBER_ATTEMPTS = 5;
    static final String ACTOR_CUSTOMER = "CLIENTE";
    static final String ACTOR_ADMIN = "ADMIN";

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final CatalogClient catalogClient;
    private final ShippingCalculator shippingCalculator;
    private final OrderNumberGenerator orderNumberGenerator;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository,
                        CatalogClient catalogClient, ShippingCalculator shippingCalculator,
                        OrderNumberGenerator orderNumberGenerator, PlatformTransactionManager transactionManager,
                        Clock clock) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.catalogClient = catalogClient;
        this.shippingCalculator = shippingCalculator;
        this.orderNumberGenerator = orderNumberGenerator;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    public OrderResponse createOrder(AuthenticatedUser user, CreateOrderRequest request) {
        List<CartItem> cart = cartItemRepository.findByUserIdOrderByCreatedAtAscIdAsc(user.id());
        if (cart.isEmpty()) {
            throw ApiException.conflict("CART_EMPTY", "El carrito está vacío");
        }
        List<StockItem> stockItems = cart.stream()
                .map(item -> new StockItem(item.getProductId(), item.getQuantity()))
                .toList();

        // Paso 1: reserva de inventario (llamada remota fuera de la transacción de base de datos).
        List<ReservedProduct> reserved = catalogClient.reserve(stockItems);
        try {
            // Paso 2: transacción local.
            Order order = transactionTemplate.execute(status -> persistOrder(user, request, reserved));
            log.info("Orden {} confirmada para el usuario id={}", order.getOrderNumber(), user.id());
            return OrderResponse.from(order);
        } catch (RuntimeException ex) {
            // Paso 3: compensación.
            log.error("Fallo al registrar la orden; se libera el inventario reservado", ex);
            releaseQuietly(stockItems);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getOrders(Long userId, int page, int size) {
        return PageResponse.from(orderRepository.findByUserId(userId, pageRequest(page, size)),
                OrderSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, String orderNumber) {
        return OrderResponse.from(findOwnOrder(userId, orderNumber));
    }

    @Transactional
    public OrderResponse cancelOrder(Long userId, String orderNumber) {
        Order order = findOwnOrder(userId, orderNumber);
        if (!order.getStatus().isCancellableByCustomer()) {
            throw ApiException.conflict("ORDER_NOT_CANCELLABLE",
                    "La orden ya no puede cancelarse porque se encuentra " + order.getStatus().label().toLowerCase());
        }
        return OrderResponse.from(transition(order, OrderStatus.CANCELLED, ACTOR_CUSTOMER));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getAllOrders(OrderStatus status, int page, int size) {
        PageRequest pageable = pageRequest(page, size);
        return PageResponse.from(status == null ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable), OrderSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getAnyOrder(String orderNumber) {
        return OrderResponse.from(findOrder(orderNumber));
    }

    @Transactional
    public OrderResponse updateStatus(String orderNumber, OrderStatus target) {
        Order order = findOrder(orderNumber);
        if (!order.getStatus().canTransitionTo(target)) {
            throw ApiException.conflict("INVALID_STATUS_TRANSITION", "No se puede cambiar una orden "
                    + order.getStatus().label().toLowerCase() + " a " + target.label().toLowerCase());
        }
        return OrderResponse.from(transition(order, target, ACTOR_ADMIN));
    }

    private Order transition(Order order, OrderStatus target, String actor) {
        order.changeStatus(target, actor, clock.instant());
        orderRepository.saveAndFlush(order); // valida el bloqueo optimista antes de tocar el inventario
        if (target == OrderStatus.CANCELLED) {
            // Si el catálogo falla se lanza 503 y se revierte la cancelación: el inventario queda consistente.
            catalogClient.release(order.getItems().stream()
                    .map(item -> new StockItem(item.getProductId(), item.getQuantity()))
                    .toList());
        }
        log.info("Orden {} cambió a {} por {}", order.getOrderNumber(), target, actor);
        return order;
    }

    private Order persistOrder(AuthenticatedUser user, CreateOrderRequest request, List<ReservedProduct> reserved) {
        String customerName = user.name() != null ? user.name() : user.email();
        Order order = new Order(uniqueOrderNumber(), user.id(), customerName, user.email(),
                request.shippingAddress().trim());
        reserved.forEach(product -> order.addItem(new OrderItem(product.productId(), product.sku(), product.name(),
                product.imageUrl(), product.unitPrice(), product.quantity())));

        BigDecimal subtotal = ShippingCalculator.money(order.getItems().stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        order.applyTotals(subtotal, shippingCalculator.shippingFor(subtotal));
        order.changeStatus(OrderStatus.CONFIRMED, ACTOR_CUSTOMER, clock.instant());

        orderRepository.save(order);
        cartItemRepository.deleteByUserId(user.id());
        return order;
    }

    private String uniqueOrderNumber() {
        for (int attempt = 0; attempt < MAX_ORDER_NUMBER_ATTEMPTS; attempt++) {
            String candidate = orderNumberGenerator.next();
            if (!orderRepository.existsByOrderNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No se pudo generar un número de orden único");
    }

    private void releaseQuietly(List<StockItem> items) {
        try {
            catalogClient.release(items);
        } catch (RuntimeException ex) {
            // Requiere conciliación manual; en producción se publicaría a una cola de reintentos (outbox).
            log.error("No se pudo liberar el inventario {}", items, ex);
        }
    }

    private Order findOwnOrder(Long userId, String orderNumber) {
        return orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
                .orElseThrow(OrderService::orderNotFound);
    }

    private Order findOrder(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).orElseThrow(OrderService::orderNotFound);
    }

    private static ApiException orderNotFound() {
        return ApiException.notFound("ORDER_NOT_FOUND", "La orden no existe");
    }

    private static PageRequest pageRequest(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    }
}
