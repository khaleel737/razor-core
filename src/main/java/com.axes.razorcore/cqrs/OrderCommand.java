package com.axes.razorcore.cqrs;

import com.axes.razorcore.core.IOrder;
import com.axes.razorcore.core.OrderAction;
import com.axes.razorcore.core.OrderType;
import com.axes.razorcore.data.L2MarketData;
import com.axes.razorcore.event.MatchTradeEventHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public final class OrderCommand implements IOrder {

public OrderCommandType commandType;
@Getter
public long orderId;
public int symbol;
@Getter
public long price;
@Getter
public long size;
@Getter
public long reserveBidPrice;
@Getter
public OrderAction action;
public OrderType orderType;
@Getter
public long uuid;
@Getter
public long timestamp;
public int userCookies;
public long eventsGroup;
public int serviceFlags;
public CommandResultCode resultCode;
public MatchTradeEventHandler matchTradeEventHandler;
public L2MarketData marketData;

public static OrderCommand newOrder(OrderType orderType, long orderId, long uuid, long price, long reserveBidPrice, long size, OrderAction orderAction) {
OrderCommand command = new OrderCommand();
command.commandType = OrderCommandType.PLACE_ORDER;
command.orderId = orderId;
command.uuid = uuid;
command.price = price;
command.reserveBidPrice = reserveBidPrice;
command.size = size;
command.action = orderAction;
command.orderType = orderType;
command.resultCode = CommandResultCode.VALID_FOR_MATCHING_ENGINE;
return command;
}

public static OrderCommand cancelOrder(long orderId, long uuid) {
    OrderCommand command = new OrderCommand();
    command.commandType = OrderCommandType.CANCEL_ORDER;
    command.orderId = orderId;
    command.uuid = uuid;
    command.resultCode = CommandResultCode.VALID_FOR_MATCHING_ENGINE;
    return command;
}

public static OrderCommand reduceOrder(long orderId, long uuid, long reduceSize) {
    OrderCommand command = new OrderCommand();
    command.commandType = OrderCommandType.REDUCE_ORDER;
    command.orderId = orderId;
    command.uuid = uuid;
    command.size = reduceSize;
    command.resultCode = CommandResultCode.VALID_FOR_MATCHING_ENGINE;
    return command;
}

public static OrderCommand updateOrder(long orderId, long uuid, long price) {
    OrderCommand command = new OrderCommand();
    command.commandType = OrderCommandType.MOVE_ORDER;
    command.orderId = orderId;
    command.uuid = uuid;
    command.price = price;
    command.resultCode = CommandResultCode.VALID_FOR_MATCHING_ENGINE;
    return command;
}

public void processMatchTradeEventHandler(Consumer<MatchTradeEventHandler> matchTradeEventHandlerConsumer) {
    MatchTradeEventHandler matchTradeEventHandler = this.matchTradeEventHandler;
    while(matchTradeEventHandler != null) {
        matchTradeEventHandlerConsumer.accept(matchTradeEventHandler);
        matchTradeEventHandler = matchTradeEventHandler.matchTradeNextEvent;
    }
}

public List<MatchTradeEventHandler> extractEvents() {
    List<MatchTradeEventHandler> matchTradeEventHandlers = new ArrayList<>();
    processMatchTradeEventHandler(matchTradeEventHandlers::add);
    return matchTradeEventHandlers;
}

public void writeTo(OrderCommand orderCommand) {
    orderCommand.commandType = this.commandType;
    orderCommand.orderId = this.orderId;
    orderCommand.symbol = this.symbol;
    orderCommand.uuid = this.uuid;
    orderCommand.timestamp = this.timestamp;
    orderCommand.reserveBidPrice = this.reserveBidPrice;
    orderCommand.price = this.price;
    orderCommand.size = this.size;
    orderCommand.action = this.action;
    orderCommand.orderType = this.orderType;
}


public OrderCommand copy() {
    OrderCommand newCommand = new OrderCommand();
    writeTo(newCommand);
    newCommand.resultCode = this.resultCode;

    List<MatchTradeEventHandler> matchTradeEvent = extractEvents();

    for(MatchTradeEventHandler matchedEvents : matchTradeEvent) {
        MatchTradeEventHandler copy = matchedEvents.copy();
        copy.matchTradeNextEvent = newCommand.matchTradeEventHandler;
        newCommand.matchTradeEventHandler = copy;
    }

    if(marketData != null) {
        newCommand.marketData = marketData.copy();
    }

    return newCommand;
}

    @Override
    public long getFilled() {
        return 0;
    }

    @Override
    public int stateHash() {
        throw new IllegalArgumentException("Command Doesnt Represent State");
    }
}

