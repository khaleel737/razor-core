package com.axes.razorcore.data;

import lombok.Data;

@Data
public class OrderCommand {
    private final String orderId;
    private final String symbol;
    private final Side side;
    private final long price;
    private final long quantity;
    private final Instant timestamp;

    public OrderCommand(String orderId, String symbol, Side side, long price, long quantity, Instant timestamp) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    // getters for all fields
}

