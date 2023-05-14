package com.axes.razorcore.core;

import com.axes.razorcore.utils.StateHash;

public interface IOrder extends StateHash {

    long getPrice();
    long getSize();
    long getFilled();
    long getUuid();
    OrderAction getAction();
    long getOrderId();
    long getTimestamp();
    long getReserveBidPrice();

}
