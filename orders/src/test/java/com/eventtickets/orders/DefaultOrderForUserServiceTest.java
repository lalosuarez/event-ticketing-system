package com.eventtickets.orders;

import com.eventtickets.orders.exception.OrderNotFoundException;
import com.eventtickets.orders.jdbc.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Window;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultOrderForUserServiceTest {

    @Mock
    private OrderForUserRepository orderForUserRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private DefaultOrderForUserService orderService;

    @Mock
    private OrderCreatedProducer orderCreatedProducer;

    @Mock
    private OrderCancelledProducer orderCancelledProducer;

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrder {

        @Test
        @DisplayName("Should successfully map request and save new order as PENDING")
        void createForUser_ValidRequest_ReturnsOrderResponse() {
            // Arrange
            var request = new CreateOrderRequest("user-123", "01a0f256-bf4e-7043-b9bc-7f45f9f8e79e");
            var expectedId = UUID.randomUUID();
            var ticketId = UUID.fromString("01a0f256-bf4e-7043-b9bc-7f45f9f8e79e");
            AggregateReference<TicketEntity, UUID> ticketIdRef = AggregateReference.to(ticketId);
            var mockTicket = new TicketEntity(ticketId, "test title", new BigDecimal("100.00"),
                    "user-123");
            var mockSavedEntity = new OrderEntity(
                    "user-123", ticketIdRef, OrderStatus.PENDING, OffsetDateTime.now().plusMinutes(15),
                    "user-123", "user-123")
                    .withId(expectedId);

            when(orderForUserRepository.save(any(OrderEntity.class))).thenReturn(mockSavedEntity);
            when(ticketRepository.findById(ticketIdRef.getId())).thenReturn(Optional.of(mockTicket));
            doNothing().when(orderCreatedProducer).send(any(OrderCreatedEvent.class));

            // Act
            OrderResponse response = orderService.createForUser(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(expectedId.toString());
            assertThat(response.status()).isEqualTo("PENDING");
            verify(orderForUserRepository).save(any(OrderEntity.class));
        }
    }

    @Nested
    @DisplayName("Get Orders Tests")
    class GetOrders {

        @Test
        @DisplayName("Should return a structured Window wrapped with offset ScrollPositions")
        void getAllForUser_ValidParameters_ReturnsWindowOfResponses() {
            // Arrange
            AggregateReference<TicketEntity, UUID> ticketId1 = AggregateReference.to(UUID.fromString("01a0f256-bf4e-7043-b9bc-7f45f9f8e79e"));
            AggregateReference<TicketEntity, UUID> ticketId2 = AggregateReference.to(UUID.randomUUID());
            String userId = "user-123";
            OrderEntity entity1 = new OrderEntity(userId, ticketId1, OrderStatus.PENDING, OffsetDateTime.now(),
                    userId, userId)
                    .withId(UUID.randomUUID());
            OrderEntity entity2 = new OrderEntity(userId, ticketId2, OrderStatus.PENDING, OffsetDateTime.now(),
                    userId, userId)
                    .withId(UUID.randomUUID());

            when(orderForUserRepository.findAllByUserId(userId, 2, 0L)).thenReturn(List.of(entity1, entity2));

            // Act
            Window<OrderResponse> resultWindow = orderService.getAllForUser(userId, 2, 0L);

            // Assert
            assertThat(resultWindow).hasSize(2);
            assertThat(resultWindow.getContent().getFirst().ticketId()).isEqualTo("01a0f256-bf4e-7043-b9bc-7f45f9f8e79e");
            verify(orderForUserRepository).findAllByUserId(userId, 2, 0L);
        }

        @Test
        @DisplayName("Should successfully return response when order is found by ID and UserID")
        void getByIdForUser_ExistingOrder_ReturnsOrderResponse() {
            // Arrange
            UUID orderId = UUID.randomUUID();
            AggregateReference<TicketEntity, UUID> ticketId = AggregateReference.to(UUID.randomUUID());
            String userId = "user-123";
            OrderEntity mockEntity = new OrderEntity(userId, ticketId, OrderStatus.PENDING,
                    OffsetDateTime.now(), userId, userId)
                    .withId(orderId);

            when(orderForUserRepository.findOneByIdAndUserId(orderId, userId)).thenReturn(mockEntity);

            // Act
            OrderResponse response = orderService.getByIdForUser(orderId.toString(), userId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(orderId.toString());
        }

        @Test
        @DisplayName("Should throw OrderNotFoundException when order does not exist")
        void getByIdForUser_NonExistentOrder_ThrowsOrderNotFoundException() {
            // Arrange
            UUID orderId = UUID.randomUUID();
            String userId = "user-123";
            when(orderForUserRepository.findOneByIdAndUserId(orderId, userId)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> orderService.getByIdForUser(orderId.toString(), userId))
                    .isInstanceOf(OrderNotFoundException.class)
                    .hasMessageContaining("Order not found");
        }
    }

    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrder {

        @Test
        @DisplayName("Should update status to CANCELLED when valid order is cancelled")
        void cancelForUser_ExistingOrder_ReturnsCancelledOrderResponse() {
            // Arrange
            UUID orderId = UUID.randomUUID();
            AggregateReference<TicketEntity, UUID> ticketId = AggregateReference.to(UUID.randomUUID());
            String userId = "user-123";

            OrderEntity existingEntity = new OrderEntity(userId, ticketId, OrderStatus.PENDING,
                    OffsetDateTime.now(), userId, userId)
                    .withId(orderId);
            OrderEntity cancelledEntity = new OrderEntity(userId, ticketId, OrderStatus.CANCELLED,
                    OffsetDateTime.now(), userId, userId)
                    .withId(orderId);

            // Your code uses entity.withStatusAndUser(...), we stub that chain's output
            when(orderForUserRepository.findOneByIdAndUserId(orderId, userId)).thenReturn(existingEntity);
            when(orderForUserRepository.save(any(OrderEntity.class))).thenReturn(cancelledEntity);
            doNothing().when(orderCancelledProducer).send(any(OrderCancelledEvent.class));

            // Act
            OrderResponse response = orderService.cancelForUser(new CancelOrderRequest(userId, orderId.toString()));

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("CANCELLED");
            verify(orderForUserRepository).save(any(OrderEntity.class));
        }
    }
}
