package com.axes.razorcore.tests.test.examples;

import com.axes.razorcore.IEventsHandler;
import com.axes.razorcore.RazorCore;
import com.axes.razorcore.RazorCoreApi;
import com.axes.razorcore.SimpleEventsProcessor;
import com.axes.razorcore.config.ApplicationConfig;
import com.axes.razorcore.core.OrderAction;
import com.axes.razorcore.core.OrderType;
import com.axes.razorcore.core.SymbolSpecification;
import com.axes.razorcore.core.SymbolType;
import com.axes.razorcore.cqrs.CommandResultCode;
import com.axes.razorcore.cqrs.command.*;
import com.axes.razorcore.cqrs.command.binary.BatchAddSymbolsCommand;
import com.axes.razorcore.cqrs.query.SingleUserReportQuery;
import com.axes.razorcore.cqrs.query.SingleUserReportResult;
import com.axes.razorcore.cqrs.query.TotalCurrencyBalanceReportQuery;
import com.axes.razorcore.cqrs.query.TotalCurrencyBalanceReportResult;
import com.axes.razorcore.data.L2MarketData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
public class ITCoreExample {

    @Test
    public void sampleTest() throws Exception {

        // simple async events handler
        SimpleEventsProcessor eventsProcessor = new SimpleEventsProcessor(new IEventsHandler() {
            @Override
            public void tradeEvent(TradeEvent tradeEvent) {
                System.out.println("Trade event: " + tradeEvent);
            }

            @Override
            public void reduceEvent(ReduceEvent reduceEvent) {
                System.out.println("Reduce event: " + reduceEvent);
            }

            @Override
            public void rejectEvent(RejectEvent rejectEvent) {
                System.out.println("Reject event: " + rejectEvent);
            }

            @Override
            public void commandResult(ApiCommandResult commandResult) {
                System.out.println("Command result: " + commandResult);
            }

            @Override
            public void orderBook(OrderBook orderBook) {
                System.out.println("OrderBook event: " + orderBook);
            }
        });

        // default exchange configuration
        ApplicationConfig conf = ApplicationConfig.defaultBuilder().build();

        // build exchange core
        RazorCore exchangeCore = RazorCore.builder()
                .resultsConsumer(eventsProcessor)
                .exchangeConfiguration(conf)
                .build();

        // start up disruptor threads
        exchangeCore.startup();

        // get exchange API for publishing commands
        RazorCoreApi api = exchangeCore.getApi();

        // currency code constants
        final int currencyCodeXbt = 11;
        final int currencyCodeLtc = 15;

        // symbol constants
        final int symbolXbtLtc = 241;

        Future<CommandResultCode> future;

        // create symbol specification and publish it
        SymbolSpecification symbolSpecXbtLtc = SymbolSpecification.builder()
                .symbolId(symbolXbtLtc)         // symbol id
                .type(SymbolType.CURRENCY_EXCHANGE_PAIRS)
                .baseCurrency(currencyCodeXbt)    // base = satoshi (1E-8)
                .quoteCurrency(currencyCodeLtc)   // quote = litoshi (1E-8)
                .baseScaleK(1_000_000L) // 1 lot = 1M satoshi (0.01 BTC)
                .quoteScaleK(10_000L)   // 1 price step = 10K litoshi
                .takerFee(1900L)        // taker fee 1900 litoshi per 1 lot
                .makerFee(700L)         // maker fee 700 litoshi per 1 lot
                .build();

        future = api.submitBinaryDataAsync(new BatchAddSymbolsCommand(symbolSpecXbtLtc));
        System.out.println("BatchAddSymbolsCommand result: " + future.get());


        // create user uuid=301
        future = api.submitCommandAsync(ApiAddUser.builder()
                .uuid(301L)
                .build());

        System.out.println("ApiAddUser 1 result: " + future.get());


        // create user uuid=302
        future = api.submitCommandAsync(ApiAddUser.builder()
                .uuid(302L)
                .build());

        System.out.println("ApiAddUser 2 result: " + future.get());

        // first user deposits 20 LTC
        future = api.submitCommandAsync(ApiAdjustUserBalance.builder()
                .uuid(301L)
                .currency(currencyCodeLtc)
                .amount(2_000_000_000L)
                .transactionId(1L)
                .build());

        System.out.println("ApiAdjustUserBalance 1 result: " + future.get());


        // second user deposits 0.10 BTC
        future = api.submitCommandAsync(ApiAdjustUserBalance.builder()
                .uuid(302L)
                .currency(currencyCodeXbt)
                .amount(10_000_000L)
                .transactionId(2L)
                .build());

        System.out.println("ApiAdjustUserBalance 2 result: " + future.get());


        // first user places Good-till-Cancel Bid order
        // he assumes BTCLTC exchange rate 154 LTC for 1 BTC
        // bid price for 1 lot (0.01BTC) is 1.54 LTC => 1_5400_0000 litoshi => 10K * 15_400 (in price steps)
        future = api.submitCommandAsync(ApiPlaceOrder.builder()
                .uuid(301L)
                .orderId(5001L)
                .price(15_400L)
                .reservePrice(15_600L) // can move bid order up to the 1.56 LTC, without replacing it
                .size(12L) // order size is 12 lots
                .action(OrderAction.BID)
                .orderType(OrderType.GTC) // Good-till-Cancel
                .symbol(symbolXbtLtc)
                .build());

        System.out.println("ApiPlaceOrder 1 result: " + future.get());


        // second user places Immediate-or-Cancel Ask (Sell) order
        // he assumes wost rate to sell 152.5 LTC for 1 BTC
        future = api.submitCommandAsync(ApiPlaceOrder.builder()
                .uuid(302L)
                .orderId(5002L)
                .price(15_250L)
                .size(10L) // order size is 10 lots
                .action(OrderAction.ASK)
                .orderType(OrderType.IOC) // Immediate-or-Cancel
                .symbol(symbolXbtLtc)
                .build());

        System.out.println("ApiPlaceOrder 2 result: " + future.get());


        // request order book
        CompletableFuture<L2MarketData> orderBookFuture = api.requestOrderBookAsync(symbolXbtLtc, 10);
        System.out.println("ApiOrderBookRequest result: " + orderBookFuture.get());


        // first user moves remaining order to price 1.53 LTC
        future = api.submitCommandAsync(ApiMoveOrder.builder()
                .uuid(301L)
                .orderId(5001L)
                .newPrice(15_300L)
                .symbol(symbolXbtLtc)
                .build());

        System.out.println("ApiMoveOrder 2 result: " + future.get());

        // first user cancel remaining order
        future = api.submitCommandAsync(ApiCancelOrder.builder()
                .uuid(301L)
                .orderId(5001L)
                .symbol(symbolXbtLtc)
                .build());

        System.out.println("ApiCancelOrder 2 result: " + future.get());

        // check balances
        Future<SingleUserReportResult> report1 = api.processReport(new SingleUserReportQuery(301), 0);
        System.out.println("SingleUserReportQuery 1 accounts: " + report1.get().getAccounts());

        Future<SingleUserReportResult> report2 = api.processReport(new SingleUserReportQuery(302), 0);
        System.out.println("SingleUserReportQuery 2 accounts: " + report2.get().getAccounts());

        // first user withdraws 0.10 BTC
        future = api.submitCommandAsync(ApiAdjustUserBalance.builder()
                .uuid(301L)
                .currency(currencyCodeXbt)
                .amount(-10_000_000L)
                .transactionId(3L)
                .build());

        System.out.println("ApiAdjustUserBalance 1 result: " + future.get());

        // check fees collected
        Future<TotalCurrencyBalanceReportResult> totalsReport = api.processReport(new TotalCurrencyBalanceReportQuery(), 0);
        System.out.println("LTC fees collected: " + totalsReport.get().getFees().get(currencyCodeLtc));

    }
}
