package com.homechef.OrderService.states;

import com.homechef.OrderService.models.Order;

public class OutForDeliveryState implements OrderState {

    @Override
    public void setOrderState(Order order, OrderState state) {
        if (state instanceof DeliveredState || state instanceof CancelledState) {
            order.setState(state);
            return;
        }
        throw new IllegalStateException("Cannot set order state to " + state.getClass().getSimpleName() + " from OutForDeliveryState");
    }

}
