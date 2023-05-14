package com.axes.razorcore.utils;

import com.axes.razorcore.cqrs.OrderCommand;

public interface SimpleEventHandler {

    boolean onEvent(long sequence, OrderCommand orderEventCommand);
}
