package com.axes.razorcore.service;

import com.axes.razorcore.cqrs.OrderCommand;
import com.axes.razorcore.cqrs.OrderCommandType;
import com.lmax.disruptor.EventHandler;
import lombok.RequiredArgsConstructor;

import java.util.function.ObjLongConsumer;

@RequiredArgsConstructor
public final class ResultsHandler implements EventHandler<OrderCommand> {

    private final ObjLongConsumer<OrderCommand> resultsConsumer;

    private boolean processingEnabled = true;

    @Override
    public void onEvent(OrderCommand cmd, long sequence, boolean endOfBatch) {

        if (cmd.commandType == OrderCommandType.GROUPING_CONTROL) {
            processingEnabled = cmd.orderId == 1;
        }

        if (processingEnabled) {
            resultsConsumer.accept(cmd, sequence);
        }

    }
}
