package com.homechef.OrderService.models;

import com.homechef.OrderService.states.*;

public enum OrderStatus {
    CREATED,
    CANCELLED,
    DELIVERED,
    OUT_FOR_DELIVERY,
    PREPARING,
    PREPARED
    ;

    public static OrderState getState(OrderStatus status) {
        return switch (status) {
            case CREATED -> new CreatedState();
            case CANCELLED -> new CancelledState();
            case DELIVERED -> new DeliveredState();
            case OUT_FOR_DELIVERY -> new OutForDeliveryState();
            case PREPARING -> new PreparingState();
            case PREPARED -> new PreparedState();
        };
    }
}
