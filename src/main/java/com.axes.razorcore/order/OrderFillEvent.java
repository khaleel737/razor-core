package com.axes.razorcore.order;

import com.axes.razorcore.data.Instant;
import com.axes.razorcore.data.Side;

public class OrderFillEvent {
    private final String orderId;
    private final String symbol;
    private final Side side;
    private final long price;
    private final long quantity;
    private final Instant timestamp;
    private final String accountId;
    private final long fillQuantity;
    private final long fillCost;

    public OrderFillEvent(String orderId, String symbol, Side side, long price, long quantity, Instant timestamp, String accountId, long fillQuantity, long fillCost) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
        this.accountId = accountId;
        this.fillQuantity = fillQuantity;
        this.fillCost = fillCost;
    }

    // getters for all fields
}

