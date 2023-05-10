package com.axes.razorcore.data;

import com.axes.razorcore.core.OrderDirection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderRequest {
    private final String orderId;
    private final String symbol;
    private final OrderDirection side;
    private final long price;
    private final long quantity;
}

