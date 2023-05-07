package com.axes.razorcore.service;


import com.axes.razorcore.data.AccountSnapshot;
import com.axes.razorcore.order.OrderFillEvent;

public interface AccountService {
    void applyOrderFill(OrderFillEvent fillEvent);
    void applyOrderReject(String orderId, String reason);
    AccountSnapshot createAccountSnapshot();
}

