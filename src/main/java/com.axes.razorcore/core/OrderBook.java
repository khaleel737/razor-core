package com.axes.razorcore.core;

//public class OrderBook {
//    private final String symbol;
//    private final NavigableMap<Long, List<Order>> bids;
//    private final NavigableMap<Long, List<Order>> asks;
//
//    public OrderBook(String symbol) {
//        this.symbol = symbol;
//        this.bids = new TreeMap<>(Comparator.reverseOrder());
//        this.asks = new TreeMap<>();
//    }
//
//    public void addOrder(OrderCommand orderCommand) {
//        // create a new Order object from the OrderCommand
//        Order order = new Order(orderCommand.getOrderId(), orderCommand.getOrderType(), orderCommand.getPrice(), orderCommand.getQuantity(), orderCommand.getTimestamp());
//
//        // add the order to the corresponding map (bids or asks) based on its side and price
//        NavigableMap<Long, List<Order>> map = order.getOrderType() == OrderDirection.BUY ? bids : asks;
//        map.computeIfAbsent(order.getPrice(), k -> new ArrayList<>()).add(order);
//
//        // match orders
//        matchOrders();
//    }
//
//    private void matchOrders() {
//        // TODO: implement matching logic and generate OrderFillEvent or OrderRejectEvent
//    }
//}

