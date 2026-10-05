package com.eventtickets.orders;

import org.springframework.data.domain.Window;

/**
 * All order operations are tied to a specific user
 */
interface OrderForUserService {

    OrderResponse createForUser(CreateOrderRequest createOrderRequest);

    Window<OrderResponse> getAllForUser(String userId, Integer limit, Long offset);

    OrderResponse getByIdForUser(String id, String userId);

    OrderResponse cancelForUser(CancelOrderRequest cancelOrderRequest);
}
