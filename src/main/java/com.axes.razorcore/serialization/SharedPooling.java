package com.axes.razorcore.serialization;

import com.axes.razorcore.event.MatchTradeEventHandler;
import lombok.Getter;

import java.util.concurrent.LinkedBlockingQueue;

public class SharedPooling {
    private final LinkedBlockingQueue<MatchTradeEventHandler> eventChainsBuffer;

    @Getter
    private final int chainLength;
    public static SharedPooling createTestSharedPool() {
        return new SharedPooling(8, 4, 256);
    }

    public SharedPooling(final int poolMaxSize, final int poolInitialSize, final int chainLength) {

        if (poolInitialSize > poolMaxSize) {
            throw new IllegalArgumentException("too big poolInitialSize");
        }

        this.eventChainsBuffer = new LinkedBlockingQueue<>(poolMaxSize);
        this.chainLength = chainLength;

        for (int i = 0; i < poolInitialSize; i++) {
            this.eventChainsBuffer.add(MatchTradeEventHandler.createChainEvent(chainLength));
        }
    }

    public MatchTradeEventHandler getChain() {
        MatchTradeEventHandler poll = eventChainsBuffer.poll();
        if (poll == null) {
            poll = MatchTradeEventHandler.createChainEvent(chainLength);
        }

        return poll;
    }

    public void putChain(MatchTradeEventHandler head) {
        boolean offer = eventChainsBuffer.offer(head);
    }
}
