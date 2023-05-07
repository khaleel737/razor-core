package com.axes.razorcore.service;

import com.axes.razorcore.core.MatchingEngine;
import com.axes.razorcore.data.Instant;
import com.axes.razorcore.data.OrderCommand;
import com.axes.razorcore.data.OrderRequest;

public class OrderServiceImpl {
    private final MatchingEngine matchingEngine;
    private final AccountService accountService;

    public OrderServiceImpl(MatchingEngine matchingEngine, AccountService accountService) {
        this.matchingEngine = matchingEngine;
        this.accountService = accountService;
    }

    public void submitOrderRequest(OrderRequest orderRequest) {
        // create an OrderCommand from the OrderRequest
        OrderCommand orderCommand = new OrderCommand(orderRequest.getOrderId(), orderRequest.getSymbol(), orderRequest.getSide(), orderRequest.getPrice(), orderRequest.getQuantity(), Instant.now());

        // publish the OrderCommand to the MatchingEngine
        matchingEngine.receiveOrder(orderCommand);
    }

    // update account balances and send response to user based on OrderFillEvent or OrderRejectEvent
}

