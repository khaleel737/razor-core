package com.axes.razorcore.core;

import com.axes.razorcore.data.OrderCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MatchingEngine {
    private final Map<String, OrderBook> orderBooks;

    public MatchingEngine() {
        this.orderBooks = new ConcurrentHashMap<>();
    }

    public void receiveOrder(OrderCommand orderCommand) {
        // find the corresponding order book for the symbol
        OrderBook orderBook = orderBooks.computeIfAbsent(orderCommand.getSymbol(), s -> new OrderBook(s));

        // add the order to the order book
        orderBook.addOrder(orderCommand);
    }

}

