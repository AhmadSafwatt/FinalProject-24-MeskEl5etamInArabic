package com.homechef.OrderService.states;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderStatus;

public class PreparedState implements  OrderState{
    @Override
    public void setOrderState(Order order, OrderState state) {
        if (state instanceof OutForDeliveryState){
            order.setState(state);
            return;
        }
        throw new IllegalStateException("Cannot change state to " + state.getClass().getSimpleName());
    }


    @Override
    public OrderStatus getOrderStatus() {
        return OrderStatus.PREPARED;
    }
}
