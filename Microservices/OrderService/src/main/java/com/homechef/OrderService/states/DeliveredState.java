package com.homechef.OrderService.states;

import com.homechef.OrderService.models.Order;

public class DeliveredState implements OrderState {
    @Override
    public void setOrderState(Order order, OrderState state) {
        throw new IllegalStateException("Cannot set order state in delivered state");
    }
}
