package com.axes.razorcore.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderRequest {
    private final String orderId;
    private final String symbol;
    private final Side side;
    private final long price;
    private final long quantity;
}

