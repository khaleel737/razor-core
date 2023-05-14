package com.axes.razorcore;

import com.axes.razorcore.cqrs.CommandResultCode;
import com.axes.razorcore.cqrs.OrderCommand;
import com.axes.razorcore.cqrs.command.*;
import com.axes.razorcore.data.L2MarketData;
import com.axes.razorcore.event.MatchEventType;
import com.axes.razorcore.event.MatchTradeEventHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.agrona.collections.MutableBoolean;
import org.agrona.collections.MutableLong;
import org.agrona.collections.MutableReference;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ObjLongConsumer;

@RequiredArgsConstructor
@Getter
@Slf4j
public class SimpleEventsProcessor implements ObjLongConsumer<OrderCommand> {

    private final IEventsHandler eventsHandler;

    @Override
    public void accept(OrderCommand cmd, long seq) {
        try {
            sendCommandResult(cmd, seq);
            sendTradeEvents(cmd);
            sendMarketData(cmd);
        } catch (Exception ex) {
            log.error("Exception when handling command result data", ex);
        }
    }

    private void sendTradeEvents(OrderCommand cmd) {
        final MatchTradeEventHandler firstEvent = cmd.matchTradeEventHandler;
        if (firstEvent == null) {
            return;
        }

        if (firstEvent.matchEventType == MatchEventType.REDUCE) {

            final IEventsHandler.ReduceEvent evt = new IEventsHandler.ReduceEvent(
                    cmd.symbol,
                    firstEvent.size,
                    firstEvent.activeOrderCompleted,
                    firstEvent.price,
                    cmd.orderId,
                    cmd.uuid,
                    cmd.timestamp);

            eventsHandler.reduceEvent(evt);

            if (firstEvent.matchTradeNextEvent != null) {
                throw new IllegalStateException("Only single REDUCE event is expected");
            }

            return;
        }

        sendTradeEvent(cmd);
    }

    private void sendTradeEvent(OrderCommand cmd) {

        final MutableBoolean takerOrderCompleted = new MutableBoolean(false);
        final MutableLong mutableLong = new MutableLong(0L);
        final List<IEventsHandler.Trade> trades = new ArrayList<>();

        final MutableReference<IEventsHandler.RejectEvent> rejectEvent = new MutableReference<>(null);

        cmd.processMatchTradeEventHandler(evt -> {

            if (evt.matchEventType == MatchEventType.TRADE) {

                final IEventsHandler.Trade trade = new IEventsHandler.Trade(
                        evt.matchedPositionsId,
                        evt.matchedPositionsUuid,
                        evt.matchedPositionsCompleted,
                        evt.price,
                        evt.size);

                trades.add(trade);
                mutableLong.value += evt.size;

                if (evt.activeOrderCompleted) {
                    takerOrderCompleted.value = true;
                }

            } else if (evt.matchEventType == MatchEventType.REJECT) {

                rejectEvent.set(new IEventsHandler.RejectEvent(
                        cmd.symbol,
                        evt.size,
                        evt.price,
                        cmd.orderId,
                        cmd.uuid,
                        cmd.timestamp));
            }
        });

        if (!trades.isEmpty()) {

            final IEventsHandler.TradeEvent evt = new IEventsHandler.TradeEvent(
                    cmd.symbol,
                    mutableLong.value,
                    cmd.orderId,
                    cmd.uuid,
                    cmd.action,
                    takerOrderCompleted.value,
                    cmd.timestamp,
                    trades);

            eventsHandler.tradeEvent(evt);
        }

        if (rejectEvent.ref != null) {
            eventsHandler.rejectEvent(rejectEvent.ref);
        }
    }

    private void sendMarketData(OrderCommand cmd) {
        final L2MarketData marketData = cmd.marketData;
        if (marketData != null) {
            final List<IEventsHandler.OrderBookRecord> asks = new ArrayList<>(marketData.askSize);
            for (int i = 0; i < marketData.askSize; i++) {
                asks.add(new IEventsHandler.OrderBookRecord(marketData.askPrices[i], marketData.askVolumes[i], (int) marketData.askOrders[i]));
            }

            final List<IEventsHandler.OrderBookRecord> bids = new ArrayList<>(marketData.bidSize);
            for (int i = 0; i < marketData.bidSize; i++) {
                bids.add(new IEventsHandler.OrderBookRecord(marketData.bidPrices[i], marketData.bidVolumes[i], (int) marketData.bidOrders[i]));
            }

            eventsHandler.orderBook(new IEventsHandler.OrderBook(cmd.symbol, asks, bids, cmd.timestamp));
        }
    }


    private void sendCommandResult(OrderCommand cmd, long seq) {

        switch (cmd.commandType) {
            case PLACE_ORDER:
                sendApiCommandResult(new ApiPlaceOrder(
                                cmd.price,
                                cmd.size,
                                cmd.orderId,
                                cmd.action,
                                cmd.orderType,
                                cmd.uuid,
                                cmd.symbol,
                                cmd.userCookies,
                                cmd.reserveBidPrice),
                        cmd.resultCode,
                        cmd.timestamp,
                        seq);
                break;

            case MOVE_ORDER:
                sendApiCommandResult(new ApiMoveOrder(cmd.orderId, cmd.price, cmd.uuid, cmd.symbol), cmd.resultCode, cmd.timestamp, seq);
                break;

            case CANCEL_ORDER:
                sendApiCommandResult(new ApiCancelOrder(cmd.orderId, cmd.uuid, cmd.symbol), cmd.resultCode, cmd.timestamp, seq);
                break;

            case REDUCE_ORDER:
                sendApiCommandResult(new ApiReduceOrder(cmd.orderId, cmd.uuid, cmd.symbol, cmd.size), cmd.resultCode, cmd.timestamp, seq);
                break;

            case ADD_USER:
                sendApiCommandResult(new ApiAddUser(cmd.uuid), cmd.resultCode, cmd.timestamp, seq);
                break;

            case BALANCE_ADJUSTMENT:
                sendApiCommandResult(new ApiAdjustUserBalance(cmd.uuid, cmd.symbol, cmd.price, cmd.orderId), cmd.resultCode, cmd.timestamp, seq);
                break;

            case BINARY_DATA_COMMAND:
                if (cmd.resultCode != CommandResultCode.ACCEPTED) {
                    sendApiCommandResult(new ApiBinaryDataCommand(cmd.userCookies, null), cmd.resultCode, cmd.timestamp, seq);
                }
                break;

            case ORDER_BOOK_REQUEST:
                sendApiCommandResult(new ApiOrderBookRequest(cmd.symbol, (int) cmd.size), cmd.resultCode, cmd.timestamp, seq);
                break;

            // TODO add rest of commands

        }

    }

    private void sendApiCommandResult(ApiCommand cmd, CommandResultCode resultCode, long timestamp, long seq) {
        cmd.timestamp = timestamp;
        final IEventsHandler.ApiCommandResult commandResult = new IEventsHandler.ApiCommandResult(cmd, resultCode, seq);
        eventsHandler.commandResult(commandResult);
    }
}
