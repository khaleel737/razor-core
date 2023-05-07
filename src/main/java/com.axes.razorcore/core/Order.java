package com.axes.razorcore.core;

import com.axes.razorcore.data.Instant;
import com.axes.razorcore.data.Side;

public class Order {
     private final String orderId;
        private final Side side;
        private final long price;
        private final long quantity;
        private final Instant timestamp;

    public Order(String orderId, Side side, long price, long quantity, Instant timestamp) {
        this.orderId = orderId;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public Side getSide() {
        return side;
    }

    public long getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    }
