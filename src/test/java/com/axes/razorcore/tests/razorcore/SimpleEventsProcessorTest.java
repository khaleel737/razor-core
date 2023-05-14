package com.axes.razorcore.tests.razorcore;

import com.axes.razorcore.IEventsHandler;
import com.axes.razorcore.SimpleEventsProcessor;
import com.axes.razorcore.core.OrderAction;
import com.axes.razorcore.core.OrderType;
import com.axes.razorcore.cqrs.CommandResultCode;
import com.axes.razorcore.cqrs.OrderCommand;
import com.axes.razorcore.cqrs.OrderCommandType;
import com.axes.razorcore.cqrs.command.ApiCancelOrder;
import com.axes.razorcore.cqrs.command.ApiPlaceOrder;
import com.axes.razorcore.cqrs.command.ApiReduceOrder;
import com.axes.razorcore.event.MatchEventType;
import com.axes.razorcore.event.MatchTradeEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public final class SimpleEventsProcessorTest {

    private SimpleEventsProcessor processor;

    @Mock
    private IEventsHandler handler;

    @Captor
    private ArgumentCaptor<IEventsHandler.ApiCommandResult> commandResultCaptor;

    @Captor
    private ArgumentCaptor<IEventsHandler.ReduceEvent> reduceEventCaptor;

    @Captor
    private ArgumentCaptor<IEventsHandler.TradeEvent> tradeEventCaptor;

    @Captor
    private ArgumentCaptor<IEventsHandler.RejectEvent> rejectEventCaptor;

    @BeforeEach
    public void before() {
        processor = new SimpleEventsProcessor(handler);
    }

    @Test
    public void shouldHandleSimpleCommand() {

        OrderCommand cmd = sampleCancelCommand();

        processor.accept(cmd, 192837L);

        verify(handler, times(1)).commandResult(commandResultCaptor.capture());
        verify(handler, never()).tradeEvent(any());
        verify(handler, never()).rejectEvent(any());
        verify(handler, never()).reduceEvent(any());

        assertThat(commandResultCaptor.getValue().getCommand(),
                Is.is(ApiCancelOrder.builder().orderId(123L).symbol(3).uuid(29851L).build()));
    }

    @Test
    public void shouldHandleWithReduceCommand() {

        OrderCommand cmd = sampleReduceCommand();

        cmd.matchTradeEventHandler = MatchTradeEventHandler.builder()
                .matchEventType(MatchEventType.REDUCE)
                .activeOrderCompleted(true)
                .price(20100L)
                .size(8272L)
                .matchTradeNextEvent(null)
                .build();

        processor.accept(cmd, 192837L);

        verify(handler, times(1)).commandResult(commandResultCaptor.capture());
        verify(handler, never()).tradeEvent(any());
        verify(handler, never()).rejectEvent(any());
        verify(handler, times(1)).reduceEvent(reduceEventCaptor.capture());

        assertThat(commandResultCaptor.getValue().getCommand(),
                Is.is(ApiReduceOrder.builder().orderId(123L).reduceSize(3200L).symbol(3).uuid(29851L).build()));

        assertThat(reduceEventCaptor.getValue().getOrderId(), Is.is(123L));
        assertThat(reduceEventCaptor.getValue().getPrice(), Is.is(20100L));
        assertThat(reduceEventCaptor.getValue().getReducedVolume(), Is.is(8272L));
        assertTrue(reduceEventCaptor.getValue().isOrderCompleted());
    }

    @Test
    public void shouldHandleWithSingleTrade() {

        OrderCommand cmd = samplePlaceOrderCommand();

        cmd.matchTradeEventHandler = MatchTradeEventHandler.builder()
                .matchEventType(MatchEventType.TRADE)
                .activeOrderCompleted(false)
                .matchedPositionsId(276810L)
                .matchedPositionsUuid(10332L)
                .matchedPositionsCompleted(true)
                .price(20100L)
                .size(8272L)
                .matchTradeNextEvent(null)
                .build();


        processor.accept(cmd, 192837L);

        verify(handler, times(1)).commandResult(commandResultCaptor.capture());
        verify(handler, never()).rejectEvent(any());
        verify(handler, never()).reduceEvent(any());
        verify(handler, times(1)).tradeEvent(tradeEventCaptor.capture());

        assertThat(commandResultCaptor.getValue().getCommand(),
                Is.is(ApiPlaceOrder.builder()
                        .orderId(123L)
                        .symbol(3)
                        .price(52200L)
                        .size(3200L)
                        .reservePrice(12800L)
                        .action(OrderAction.BID)
                        .orderType(OrderType.IOC)
                        .uuid(29851)
                        .userCookies(44188)
                        .build()));

        IEventsHandler.TradeEvent tradeEvent = tradeEventCaptor.getValue();
        assertThat(tradeEvent.getSymbol(), Is.is(3));
        assertThat(tradeEvent.getTotalVolume(), Is.is(8272L));
        assertThat(tradeEvent.getTakerOrderId(), Is.is(123L));
        assertThat(tradeEvent.getTakerUid(), Is.is(29851L));
        assertThat(tradeEvent.getTakerAction(), Is.is(OrderAction.BID));
        assertFalse(tradeEvent.isTakeOrderCompleted());

        List<IEventsHandler.Trade> trades = tradeEvent.getTrades();
        assertThat(trades.size(), Is.is(1));
        IEventsHandler.Trade trade = trades.get(0);

        assertThat(trade.getMakerOrderId(), Is.is(276810L));
        assertThat(trade.getMakerUid(), Is.is(10332L));
        assertTrue(trade.isMakerOrderCompleted());
        assertThat(trade.getPrice(), Is.is(20100L));
        assertThat(trade.getVolume(), Is.is(8272L));
    }


    @Test
    public void shouldHandleWithTwoTrades() {

        OrderCommand cmd = samplePlaceOrderCommand();

        MatchTradeEventHandler firstTrade = MatchTradeEventHandler.builder()
                .matchEventType(MatchEventType.TRADE)
                .activeOrderCompleted(false)
                .matchedPositionsId(276810L)
                .matchedPositionsUuid(10332L)
                .matchedPositionsCompleted(true)
                .price(20100L)
                .size(8272L)
                .matchTradeNextEvent(null)
                .build();

        MatchTradeEventHandler secondTrade = MatchTradeEventHandler.builder()
                .matchEventType(MatchEventType.TRADE)
                .activeOrderCompleted(true)
                .matchedPositionsId(100293L)
                .matchedPositionsUuid(1982L)
                .matchedPositionsCompleted(false)
                .price(20110L)
                .size(3121L)
                .matchTradeNextEvent(null)
                .build();

        cmd.matchTradeEventHandler = firstTrade;
        firstTrade.matchTradeNextEvent = secondTrade;

        processor.accept(cmd, 12981721239L);

        verify(handler, times(1)).commandResult(commandResultCaptor.capture());
        verify(handler, never()).rejectEvent(any());
        verify(handler, never()).reduceEvent(any());
        verify(handler, times(1)).tradeEvent(tradeEventCaptor.capture());

        assertThat(commandResultCaptor.getValue().getCommand(),
                Is.is(ApiPlaceOrder.builder()
                        .orderId(123L)
                        .symbol(3)
                        .price(52200L)
                        .size(3200L)
                        .reservePrice(12800L)
                        .action(OrderAction.BID)
                        .orderType(OrderType.IOC)
                        .uuid(29851)
                        .userCookies(44188)
                        .build()));

        // validating first event
        IEventsHandler.TradeEvent tradeEvent = tradeEventCaptor.getAllValues().get(0);
        assertThat(tradeEvent.getSymbol(), Is.is(3));
        assertThat(tradeEvent.getTotalVolume(), Is.is(11393L));
        assertThat(tradeEvent.getTakerOrderId(), Is.is(123L));
        assertThat(tradeEvent.getTakerUid(), Is.is(29851L));
        assertThat(tradeEvent.getTakerAction(), Is.is(OrderAction.BID));
        assertTrue(tradeEvent.isTakeOrderCompleted());

        List<IEventsHandler.Trade> trades = tradeEvent.getTrades();
        assertThat(trades.size(), Is.is(2));

        IEventsHandler.Trade trade = trades.get(0);
        assertThat(trade.getMakerOrderId(), Is.is(276810L));
        assertThat(trade.getMakerUid(), Is.is(10332L));
        assertTrue(trade.isMakerOrderCompleted());
        assertThat(trade.getPrice(), Is.is(20100L));
        assertThat(trade.getVolume(), Is.is(8272L));

        trade = trades.get(1);
        assertThat(trade.getMakerOrderId(), Is.is(100293L));
        assertThat(trade.getMakerUid(), Is.is(1982L));
        assertFalse(trade.isMakerOrderCompleted());
        assertThat(trade.getPrice(), Is.is(20110L));
        assertThat(trade.getVolume(), Is.is(3121L));
    }

    @Test
    public void shouldHandleWithTwoTradesAndReject() {

        OrderCommand cmd = samplePlaceOrderCommand();

        MatchTradeEventHandler firstTrade = MatchTradeEventHandler.builder()
                .matchEventType(MatchEventType.TRADE)
                .activeOrderCompleted(false)
                .matchedPositionsId(276810L)
                .matchedPositionsUuid(10332L)
                .matchedPositionsCompleted(true)
                .price(20100L)
                .size(8272L)
                .matchTradeNextEvent(null)
                .build();

        MatchTradeEventHandler secondTrade = MatchTradeEventHandler.builder()
                .matchEventType(MatchEventType.TRADE)
                .activeOrderCompleted(true)
                .matchedPositionsId(100293L)
                .matchedPositionsUuid(1982L)
                .matchedPositionsCompleted(false)
                .price(20110L)
                .size(3121L)
                .matchTradeNextEvent(null)
                .build();

        MatchTradeEventHandler reject = MatchTradeEventHandler.builder()
                .matchEventType(MatchEventType.REJECT)
                .activeOrderCompleted(true)
                .size(8272L)
                .matchTradeNextEvent(null)
                .build();

        cmd.matchTradeEventHandler = firstTrade;
        firstTrade.matchTradeNextEvent = secondTrade;
        secondTrade.matchTradeNextEvent = reject;

        processor.accept(cmd, 12981721239L);

        verify(handler, times(1)).commandResult(commandResultCaptor.capture());
        verify(handler, times(1)).rejectEvent(rejectEventCaptor.capture());
        verify(handler, never()).reduceEvent(any());
        verify(handler, times(1)).tradeEvent(tradeEventCaptor.capture());

        assertThat(commandResultCaptor.getValue().getCommand(),
                Is.is(ApiPlaceOrder.builder()
                        .orderId(123L)
                        .symbol(3)
                        .price(52200L)
                        .size(3200L)
                        .reservePrice(12800L)
                        .action(OrderAction.BID)
                        .orderType(OrderType.IOC)
                        .uuid(29851)
                        .userCookies(44188)
                        .build()));

        // validating first event
        IEventsHandler.TradeEvent tradeEvent = tradeEventCaptor.getAllValues().get(0);
        assertThat(tradeEvent.getSymbol(), Is.is(3));
        assertThat(tradeEvent.getTotalVolume(), Is.is(11393L));
        assertThat(tradeEvent.getTakerOrderId(), Is.is(123L));
        assertThat(tradeEvent.getTakerUid(), Is.is(29851L));
        assertThat(tradeEvent.getTakerAction(), Is.is(OrderAction.BID));
        assertTrue(tradeEvent.isTakeOrderCompleted());

        List<IEventsHandler.Trade> trades = tradeEvent.getTrades();
        assertThat(trades.size(), Is.is(2));

        IEventsHandler.Trade trade = trades.get(0);
        assertThat(trade.getMakerOrderId(), Is.is(276810L));
        assertThat(trade.getMakerUid(), Is.is(10332L));
        assertTrue(trade.isMakerOrderCompleted());
        assertThat(trade.getPrice(), Is.is(20100L));
        assertThat(trade.getVolume(), Is.is(8272L));

        trade = trades.get(1);
        assertThat(trade.getMakerOrderId(), Is.is(100293L));
        assertThat(trade.getMakerUid(), Is.is(1982L));
        assertFalse(trade.isMakerOrderCompleted());
        assertThat(trade.getPrice(), Is.is(20110L));
        assertThat(trade.getVolume(), Is.is(3121L));
    }


    @Test
    public void shouldHandlerWithSingleReject() {

        OrderCommand cmd = samplePlaceOrderCommand();

        cmd.matchTradeEventHandler = MatchTradeEventHandler.builder()
                .matchEventType(MatchEventType.REJECT)
                .activeOrderCompleted(true)
                .size(8272L)
                .price(52201L)
                .matchTradeNextEvent(null)
                .build();

        processor.accept(cmd, 192837L);

        verify(handler, times(1)).commandResult(commandResultCaptor.capture());
        verify(handler, never()).tradeEvent(any());
        verify(handler, never()).reduceEvent(any());
        verify(handler, times(1)).rejectEvent(rejectEventCaptor.capture());

        assertThat(commandResultCaptor.getValue().getCommand(),
                Is.is(ApiPlaceOrder.builder()
                        .orderId(123L)
                        .symbol(3)
                        .price(52200L)
                        .size(3200L)
                        .reservePrice(12800L)
                        .action(OrderAction.BID)
                        .orderType(OrderType.IOC)
                        .uuid(29851L)
                        .userCookies(44188)
                        .build()));

        IEventsHandler.RejectEvent rejectEvent = rejectEventCaptor.getValue();
        assertThat(rejectEvent.getSymbol(), Is.is(3));
        assertThat(rejectEvent.getOrderId(), Is.is(123L));
        assertThat(rejectEvent.getRejectedVolume(), Is.is(8272L));
        assertThat(rejectEvent.getPrice(), Is.is(52201L));
        assertThat(rejectEvent.getUid(), Is.is(29851L));
    }


    private OrderCommand sampleCancelCommand() {

        return OrderCommand.builder()
                .commandType(OrderCommandType.CANCEL_ORDER)
                .orderId(123L)
                .symbol(3)
                .price(12800L)
                .size(3L)
                .reserveBidPrice(12800L)
                .action(OrderAction.BID)
                .orderType(OrderType.GTC)
                .uuid(29851L)
                .timestamp(1578930983745201L)
                .userCookies(44188)
                .resultCode(CommandResultCode.MATCHING_INVALID_ORDER_BOOK_ID)
                .matchTradeEventHandler(null)
                .marketData(null)
                .build();
    }


    private OrderCommand sampleReduceCommand() {

        return OrderCommand.builder()
                .commandType(OrderCommandType.REDUCE_ORDER)
                .orderId(123L)
                .symbol(3)
                .price(52200L)
                .size(3200L)
                .reserveBidPrice(12800L)
                .action(OrderAction.BID)
                .orderType(OrderType.GTC)
                .uuid(29851L)
                .timestamp(1578930983745201L)
                .userCookies(44188)
                .resultCode(CommandResultCode.SUCCESS)
                .matchTradeEventHandler(null)
                .marketData(null)
                .build();
    }

    private OrderCommand samplePlaceOrderCommand() {

        return OrderCommand.builder()
                .commandType(OrderCommandType.PLACE_ORDER)
                .orderId(123L)
                .symbol(3)
                .price(52200L)
                .size(3200L)
                .reserveBidPrice(12800L)
                .action(OrderAction.BID)
                .orderType(OrderType.IOC)
                .uuid(29851L)
                .timestamp(1578930983745201L)
                .userCookies(44188)
                .resultCode(CommandResultCode.SUCCESS)
                .matchTradeEventHandler(null)
                .marketData(null)
                .build();
    }

    private void verifyOriginalFields(OrderCommand source, OrderCommand result) {

        assertThat(source.commandType, Is.is(result.commandType));
        assertThat(source.orderId, Is.is(result.orderId));
        assertThat(source.symbol, Is.is(result.symbol));
        assertThat(source.price, Is.is(result.price));
        assertThat(source.size, Is.is(result.size));
        assertThat(source.reserveBidPrice, Is.is(result.reserveBidPrice));
        assertThat(source.action, Is.is(result.action));
        assertThat(source.orderType, Is.is(result.orderType));
        assertThat(source.uuid, Is.is(result.uuid));
        assertThat(source.timestamp, Is.is(result.timestamp));
        assertThat(source.userCookies, Is.is(result.userCookies));
        assertThat(source.resultCode, Is.is(result.resultCode));
    }

}