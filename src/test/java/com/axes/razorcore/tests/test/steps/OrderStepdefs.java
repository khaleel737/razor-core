package com.axes.razorcore.tests.test.steps;

import com.axes.razorcore.config.PerformanceConfiguration;
import com.axes.razorcore.core.Order;
import com.axes.razorcore.core.OrderAction;
import com.axes.razorcore.core.OrderType;
import com.axes.razorcore.core.SymbolSpecification;
import com.axes.razorcore.cqrs.CommandResultCode;
import com.axes.razorcore.cqrs.OrderCommandType;
import com.axes.razorcore.cqrs.command.*;
import com.axes.razorcore.cqrs.query.SingleUserReportResult;
import com.axes.razorcore.event.MatchEventType;
import com.axes.razorcore.event.MatchTradeEventHandler;
import com.axes.razorcore.tests.test.util.ExchangeTestContainer;
import com.axes.razorcore.tests.test.util.L2MarketDataHelper;
import com.axes.razorcore.tests.test.util.TestConstants;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axes.razorcore.tests.test.util.ExchangeTestContainer.CHECK_SUCCESS;
import static com.axes.razorcore.tests.test.util.TestConstants.SYMBOLSPEC_ETH_XBT;
import static com.axes.razorcore.tests.test.util.TestConstants.SYMBOLSPEC_EUR_USD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class OrderStepdefs implements En {

    public static PerformanceConfiguration testPerformanceConfiguration = null;

    private ExchangeTestContainer container = null;

    private List<MatchTradeEventHandler> matcherEvents;
    private Map<Long, ApiPlaceOrder> orders = new HashMap<>();

    final Map<String, SymbolSpecification> symbolSpecificationMap = new HashMap<>();
    final Map<String, Long> users = new HashMap<>();

    @BeforeEach
    public void before() {
        container = ExchangeTestContainer.create(testPerformanceConfiguration);
        container.initBasicSymbols();
    }

    @AfterEach
    public void after() {
        if (container != null) {
            container.close();
        }
    }

    public OrderStepdefs() {
        symbolSpecificationMap.put("EUR_USD", SYMBOLSPEC_EUR_USD);
        symbolSpecificationMap.put("ETH_XBT", SYMBOLSPEC_ETH_XBT);
        users.put("Alice", 1440001L);
        users.put("Bob", 1440002L);
        users.put("Charlie", 1440003L);

        ParameterType(
            "symbol",
            "EUR_USD|ETH_XBT",
            symbolSpecificationMap::get
        );
        ParameterType("user",
            "Alice|Bob|Charlie",
            users::get);

        DataTableType((DataTable table) -> {
            DataTable subTable = table.rows(0);
            //skip a header if it presents
            if (table.row(0).get(0) != null && table.row(0).get(0).trim().equals("bid")) {
                subTable = table.rows(1);
            }
            //format | bid | price | ask |
            final L2MarketDataHelper l2helper = new L2MarketDataHelper();
            for (int i = 0; i < subTable.height(); i++) {
                List<String> row = subTable.row(i);
                int price = Integer.parseInt(row.get(1));

                String bid = row.get(0);
                if (bid != null && bid.length() > 0) {
                    l2helper.addBid(price, Integer.parseInt(bid));
                } else {
                    l2helper.addAsk(price, Integer.parseInt(row.get(2)));
                }
            }
            return l2helper;
        });

        Before((HookNoArgsBody) -> {
            container = ExchangeTestContainer.create(testPerformanceConfiguration);
            container.initBasicSymbols();
        });
        After((HookNoArgsBody) -> {
            if (container != null) {
                container.close();
            }
        });

        Given("New client {user} has a balance:",
            (Long clientId, DataTable table) -> {
                List<List<String>> balance = table.asLists();

                final List<ApiCommand> cmds = new ArrayList<>();

                cmds.add(ApiAddUser.builder().uuid(clientId).build());

                int transactionId = 0;

                for (List<String> entry : balance) {
                    transactionId++;
                    cmds.add(ApiAdjustUserBalance.builder().uuid(clientId).transactionId(transactionId)
                        .amount(Long.parseLong(entry.get(1)))
                        .currency(TestConstants.getCurrency(entry.get(0)))
                        .build());
                }

                container.getApi().submitCommandsSync(cmds);

            });

        When("A client {user} places an {word} order {long} at {long}@{long} \\(type: {word}, symbol: {symbol})",
            (Long clientId, String side, Long orderId, Long price, Long size, String orderType, SymbolSpecification symbol) -> {
                aClientPassAnOrder(clientId, side, orderId, price, size, orderType, symbol, 0,
                    CommandResultCode.SUCCESS);
            });

        When(
            "A client {user} places an {word} order {long} at {long}@{long} \\(type: {word}, symbol: {symbol}, reservePrice: {long})",
            (Long clientId, String side, Long orderId, Long price, Long size, String orderType, SymbolSpecification symbol, Long reservePrice) -> {
                aClientPassAnOrder(clientId, side, orderId, price, size, orderType, symbol, reservePrice,
                    CommandResultCode.SUCCESS);
            });
        Then("The order {long} is partially matched. LastPx: {long}, LastQty: {long}",
            (Long orderId, Long lastPx, Long lastQty) -> {
                theOrderIsMatched(orderId, lastPx, lastQty, false, null);
            });
        Then("The order {long} is fully matched. LastPx: {long}, LastQty: {long}, bidderHoldPrice: {long}",
            (Long orderId, Long lastPx, Long lastQty, Long bidderHoldPrice) -> {
                theOrderIsMatched(orderId, lastPx, lastQty, true, bidderHoldPrice);
            });
        And("No trade events", () -> {
            assertEquals(0, matcherEvents.size());
        });

        When("A client {user} moves a price to {long} of the order {long}",
            (Long clientId, Long newPrice, Long orderId) -> {
                moveOrder(clientId, newPrice, orderId, CommandResultCode.SUCCESS);
            });

        When("A client {user} could not move a price to {long} of the order {long} due to {word}",
            (Long clientId, Long newPrice, Long orderId, String resultCode) -> {
                moveOrder(clientId, newPrice, orderId, CommandResultCode.valueOf(resultCode));
            });

        Then("The order {long} is fully matched. LastPx: {long}, LastQty: {long}",
            (Long orderId, Long lastPx, Long lastQty) -> {
                theOrderIsMatched(orderId, lastPx, lastQty, true, null);
            });

        Then("An {symbol} order book is:",
            (SymbolSpecification symbol, L2MarketDataHelper orderBook) -> {
                assertEquals(orderBook.build(), container.requestCurrentOrderBook(symbol.symbolId));
            });

        When(
            "A client {user} could not place an {word} order {long} at {long}@{long} \\(type: {word}, symbol: {symbol}, reservePrice: {long}) due to {word}",
            (Long clientId, String side, Long orderId, Long price, Long size,
                String orderType, SymbolSpecification symbol, Long reservePrice, String resultCode) -> {
                aClientPassAnOrder(clientId, side, orderId, price, size, orderType, symbol, reservePrice,
                    CommandResultCode.valueOf(resultCode));
            });

        And("A balance of a client {user}:",
            (Long clientId, DataTable table) -> {
                List<List<String>> balance = table.asLists();
                SingleUserReportResult profile = container.getUserProfile(clientId);
                for (List<String> record : balance) {
                    assertThat("Unexpected balance of: " + record.get(0),
                        profile.getAccounts().get(TestConstants.getCurrency(record.get(0))),
                        is(Long.parseLong(record.get(1))));
                }
            });

        And("A client {user} orders:", (Long clientId, DataTable table) -> {
            List<List<String>> lists = table.asLists();
            //| id | price | size | filled | reservePrice | side |

            SingleUserReportResult profile = container.getUserProfile(clientId);

            //skip a header if it presents
            Map<String, Integer> fieldNameByIndex = new HashMap<>();

            //read a header
            int i = 0;
            for (String field : lists.get(0)) {
                fieldNameByIndex.put(field, i++);
            }

            //remove header
            lists = lists.subList(1, lists.size());

            Map<Long, Order> orders = profile.fetchIndexedOrders();

            for (List<String> record : lists) {
                long orderId = Long.parseLong(record.get(fieldNameByIndex.get("id")));
                Order order = orders.get(orderId);
                assertNotNull(order);

                checkField(fieldNameByIndex, record, "price", order.getPrice());
                checkField(fieldNameByIndex, record, "size", order.getSize());
                checkField(fieldNameByIndex, record, "filled", order.getFilled());
                checkField(fieldNameByIndex, record, "reservePrice", order.getReserveBidPrice());

                if (fieldNameByIndex.containsKey("side")) {
                    OrderAction action = OrderAction.valueOf(record.get(fieldNameByIndex.get("side")));
                    assertEquals(action, order.getAction(), "Unexpected action");
                }

            }
        });

        And("A client {user} does not have active orders", (Long clientId) -> {
            SingleUserReportResult profile = container.getUserProfile(clientId);
            assertEquals(0, profile.fetchIndexedOrders().size());
        });

        Given("{long} {word} is added to the balance of a client {user}",
            (Long ammount, String currency, Long clientId) -> {

                // add 1 szabo more
                container.submitCommandSync(ApiAdjustUserBalance.builder()
                    .uuid(clientId)
                    .currency(TestConstants.getCurrency(currency))
                    .amount(ammount).transactionId(2193842938742L).build(), CHECK_SUCCESS);
            });

        When("A client {user} cancels the remaining size {long} of the order {long}",
            (Long clientId, Long size, Long orderId) -> {
                ApiPlaceOrder initialOrder = orders.get(orderId);

                ApiCancelOrder order = ApiCancelOrder.builder().orderId(orderId).uuid(clientId)
                    .symbol(initialOrder.symbol)
                    .build();

                container.getApi().submitCommandAsyncFullResponse(order).thenAccept(
                    cmd -> {
                        assertThat(cmd.resultCode, is(CommandResultCode.SUCCESS));
                        assertThat(cmd.commandType, is(OrderCommandType.CANCEL_ORDER));
                        assertThat(cmd.orderId, is(orderId));
                        assertThat(cmd.uuid, is(clientId));
                        assertThat(cmd.symbol, is(initialOrder.symbol));
                        assertThat(cmd.action, is(initialOrder.action));

                        final MatchTradeEventHandler evt = cmd.matchTradeEventHandler;
                        assertNotNull(evt);
                        assertThat(evt.matchEventType, is(MatchEventType.REDUCE));
                        assertThat(evt.size, is(size));
                    }).join();
            });
    }

    private void aClientPassAnOrder(long clientId, String side, long orderId, long price, long size, String orderType,
        SymbolSpecification symbol, long reservePrice, CommandResultCode resultCode) {

        ApiPlaceOrder.ApiPlaceOrderBuilder builder = ApiPlaceOrder.builder().uuid(clientId).orderId(orderId).price(price)
            .size(size)
            .action(OrderAction.valueOf(side)).orderType(OrderType.valueOf(orderType))
            .symbol(symbol.symbolId);

        if (reservePrice > 0) {
            builder.reservePrice(reservePrice);
        }

        final ApiPlaceOrder order = builder.build();

        orders.put(orderId, order);

        log.debug("PLACE : {}", order);
        container.getApi().submitCommandAsyncFullResponse(order).thenAccept(cmd -> {
            assertThat(cmd.orderId, is(orderId));
            assertThat(cmd.resultCode, is(resultCode));
            assertThat(cmd.uuid, is(clientId));
            assertThat(cmd.price, is(price));
            assertThat(cmd.size, is(size));
            assertThat(cmd.action, is(OrderAction.valueOf(side)));
            assertThat(cmd.orderType, is(OrderType.valueOf(orderType)));
            assertThat(cmd.symbol, is(symbol.symbolId));

            OrderStepdefs.this.matcherEvents = cmd.extractEvents();
        }).join();
    }

    private void theOrderIsMatched(long orderId, long lastPx, long lastQty, boolean completed, Long bidderHoldPrice) {
        assertThat(matcherEvents.size(), is(1));

        MatchTradeEventHandler evt = matcherEvents.get(0);
        assertThat(evt.matchedPositionsId, is(orderId));
        assertThat(evt.matchedPositionsUuid, is(orders.get(orderId).uuid));
        assertThat(evt.matchedPositionsCompleted, is(completed));
        assertThat(evt.matchEventType, is(MatchEventType.TRADE));
        assertThat(evt.size, is(lastQty));
        assertThat(evt.price, is(lastPx));
        if (bidderHoldPrice != null) {
            assertThat(evt.bidderHoldPrice, is(bidderHoldPrice));
        }
    }

    private void moveOrder(long clientId, long newPrice, long orderId, CommandResultCode resultCode2) {
        ApiPlaceOrder initialOrder = orders.get(orderId);

        final ApiMoveOrder moveOrder = ApiMoveOrder.builder().symbol(initialOrder.symbol).uuid(clientId).orderId(orderId)
            .newPrice(newPrice).build();
        log.debug("MOVE : {}", moveOrder);
        container.submitCommandSync(moveOrder, cmd -> {
            assertThat(cmd.resultCode, is(resultCode2));
            assertThat(cmd.orderId, is(orderId));
            assertThat(cmd.uuid, is(clientId));

            matcherEvents = cmd.extractEvents();
        });
    }

    private void checkField(Map<String, Integer> fieldNameByIndex, List<String> record, String field, long expected) {
        if (fieldNameByIndex.containsKey(field)) {
            long actual = Long.parseLong(record.get(fieldNameByIndex.get(field)));
            assertEquals(actual, expected, "Unexpected value for " + field);
        }
    }

}
