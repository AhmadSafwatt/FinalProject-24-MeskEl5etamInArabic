package com.homechef.OrderService.states;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderStatus;

public class OutForDeliveryState implements OrderState {

    @Override
    public void setOrderState(Order order, OrderState state) {
        if (state instanceof DeliveredState) {
            order.setState(state);
            return;
        }
        throw new IllegalStateException(
                "Cannot set order state to " + state.getClass().getSimpleName() + " from OutForDeliveryState");
    }

    @Override
    public OrderStatus getOrderStatus() {
        return OrderStatus.OUT_FOR_DELIVERY;
    }

}
