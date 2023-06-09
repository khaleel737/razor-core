/*
 * Copyright 2019 Maksim Zheravin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.axes.razorcore.tests.test.perf.modules;

import com.axes.razorcore.config.LoggingConfiguration;
import com.axes.razorcore.orderbook.IOrderBook;
import com.axes.razorcore.orderbook.OrderBookDirectImpl;
import com.axes.razorcore.orderbook.OrderBookEventsHelper;
import com.axes.razorcore.tests.test.util.TestConstants;
import exchange.core2.collections.objpool.ObjectsPool;

public class ITOrderBookDirectImpl extends ITOrderBookBase {

    @Override
    protected IOrderBook createNewOrderBook() {

        return new OrderBookDirectImpl(
                TestConstants.SYMBOLSPEC_EUR_USD,
                ObjectsPool.createDefaultTestPool(),
                OrderBookEventsHelper.NON_POOLED_EVENTS_HELPER,
                LoggingConfiguration.DEFAULT);
    }
}
