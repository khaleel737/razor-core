package com.axes.razorcore.data;

import com.axes.razorcore.core.OrderDirection;
import lombok.Data;

@Data
public class OrderCommand {
    private final String orderId;
    private final String symbol;
    private final OrderDirection side;
    private final long price;
    private final long quantity;
    private final Instant timestamp;

    public OrderCommand(String orderId, String symbol, OrderDirection side, long price, long quantity, Instant timestamp) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }
}

