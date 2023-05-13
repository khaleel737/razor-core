package com.axes.razorcore.utils;

import com.axes.razorcore.data.OrderCommand;

public interface SimpleEventHandler {

    boolean onEvent(long sequence, OrderCommand orderEventCommand);
}
