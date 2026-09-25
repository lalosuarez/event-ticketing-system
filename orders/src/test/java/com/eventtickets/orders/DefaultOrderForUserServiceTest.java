package com.eventtickets.orders;

import com.eventtickets.orders.jdbc.OrderEntity;
import com.eventtickets.orders.jdbc.OrderForUserRepository;
import com.eventtickets.orders.jdbc.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Window;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultOrderForUserServiceTest {

    @Mock
    private OrderForUserRepository orderForUserRepository;

    @InjectMocks
    private DefaultOrderForUserService orderService;

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrder {

        @Test
        @DisplayName("Should successfully map request and save new order as PENDING")
        void createForUser_ValidRequest_ReturnsOrderResponse() {
            // Arrange
            OrderRequest request = new OrderRequest("user-123", "ticket-789");
            UUID expectedId = UUID.randomUUID();
            OrderEntity mockSavedEntity = new OrderEntity(
                    "user-123", "ticket-789", OrderStatus.PENDING, OffsetDateTime.now().plusMinutes(15),
                    "user-123", "user-123")
                    .withId(expectedId);

            when(orderForUserRepository.save(any(OrderEntity.class))).thenReturn(mockSavedEntity);

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
            String userId = "user-123";
            OrderEntity entity1 = new OrderEntity(userId, "t-1", OrderStatus.PENDING, OffsetDateTime.now(),
                    userId, userId)
                    .withId(UUID.randomUUID());
            OrderEntity entity2 = new OrderEntity(userId, "t-2", OrderStatus.PENDING, OffsetDateTime.now(),
                    userId, userId)
                    .withId(UUID.randomUUID());

            when(orderForUserRepository.findAllByUserId(userId, 2, 0L)).thenReturn(List.of(entity1, entity2));

            // Act
            Window<OrderResponse> resultWindow = orderService.getAllForUser(userId, 2, 0L);

            // Assert
            assertThat(resultWindow).hasSize(2);
            assertThat(resultWindow.getContent().getFirst().ticketId()).isEqualTo("t-1");
            verify(orderForUserRepository).findAllByUserId(userId, 2, 0L);
        }

        @Test
        @DisplayName("Should successfully return response when order is found by ID and UserID")
        void getByIdForUser_ExistingOrder_ReturnsOrderResponse() {
            // Arrange
            UUID orderId = UUID.randomUUID();
            String userId = "user-123";
            OrderEntity mockEntity = new OrderEntity(userId, "ticket-1", OrderStatus.PENDING,
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
                    .hasMessageContaining("Order " + orderId + " not found");
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
            String userId = "user-123";

            OrderEntity existingEntity = new OrderEntity(userId, "ticket-1", OrderStatus.PENDING,
                    OffsetDateTime.now(), userId, userId)
                    .withId(orderId);
            OrderEntity cancelledEntity = new OrderEntity(userId, "ticket-1", OrderStatus.CANCELLED,
                    OffsetDateTime.now(), userId, userId)
                    .withId(orderId);

            // Your code uses entity.withStatusAndUser(...), we stub that chain's output
            when(orderForUserRepository.findOneByIdAndUserId(orderId, userId)).thenReturn(existingEntity);
            when(orderForUserRepository.save(any(OrderEntity.class))).thenReturn(cancelledEntity);

            // Act
            OrderResponse response = orderService.cancelForUser(orderId.toString(), userId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("CANCELLED");
            verify(orderForUserRepository).save(any(OrderEntity.class));
        }
    }
}
