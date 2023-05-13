package com.axes.razorcore.utils;

import com.axes.razorcore.config.RazorCoreWaitStrategy;
import com.axes.razorcore.data.OrderCommand;
import com.lmax.disruptor.*;

import java.util.concurrent.atomic.AtomicInteger;

public class TwoStepSlaveProcessor implements EventProcessor {

    private static final int IDLE = 0;
    private static final int HALTED = IDLE + 1;
    private static final int RUNNING = HALTED + 1;

    private final AtomicInteger running = new AtomicInteger(IDLE);
    private final DataProvider<OrderCommand> dataProvider;
    private final SequenceBarrier sequenceBarrier;
    private final WaitSpinningHelper waitSpinningHelper;

    private final SimpleEventHandler eventHandler;
    private final Sequence sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
    private final ExceptionHandler<? super OrderCommand> exceptionHandler;
    private final String name;

    private long nextSequence = -1;

    public TwoStepSlaveProcessor(RingBuffer<OrderCommand> ringBuffer,
                                 SequenceBarrier sequenceBarrier,
                                 WaitSpinningHelper waitSpinningHelper,
                                 SimpleEventHandler eventHandler,
                                 ExceptionHandler<? super OrderCommand> exceptionHandler,
                                 String name) {
        this.dataProvider = ringBuffer;
        this.sequenceBarrier = sequenceBarrier;
        this.waitSpinningHelper = new WaitSpinningHelper(ringBuffer, sequenceBarrier, 0, RazorCoreWaitStrategy.SECOND_STEP_NO_WAIT);
        this.eventHandler = eventHandler;
        this.exceptionHandler = exceptionHandler;
        this.name = name;
    }

    @Override
    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public void halt() {
        running.set(HALTED);
        sequenceBarrier.alert();
    }

    @Override
    public boolean isRunning() {
        return running.get() != IDLE;
    }

    @Override
    public void run() {
        if (running.compareAndSet(IDLE, RUNNING)) {
            sequenceBarrier.clearAlert();
        } else if (running.get() == RUNNING) {
            throw new IllegalStateException("Thread is already running (S)");
        }

        nextSequence = sequence.get() + 1L;
    }

    public void handlingCycle(final long processUpToSequence) {
        while (true) {
            OrderCommand event = null;
            try {
                long availableSequence = waitSpinningHelper.tryWaitFor(nextSequence);

                // process batch
                while (nextSequence <= availableSequence && nextSequence < processUpToSequence) {
                    event = dataProvider.get(nextSequence);
                    eventHandler.onEvent(nextSequence, event); // TODO check if nextSequence is correct (not nextSequence+-1)?
                    nextSequence++;
                }

                // exit if finished processing entire group (up to specified sequence)
                if (nextSequence == processUpToSequence) {
                    sequence.set(processUpToSequence - 1);
                    waitSpinningHelper.signalAllWhenBlocking();
                    return;
                }

            } catch (final Throwable ex) {
                exceptionHandler.handleEventException(ex, nextSequence, event);
                sequence.set(nextSequence);
                waitSpinningHelper.signalAllWhenBlocking();
                nextSequence++;
            }
        }
    }

    @Override
    public String toString() {
        return "TwoStepSlaveProcessor{" +
                "running=" + running +
                ", dataProvider=" + dataProvider +
                ", sequenceBarrier=" + sequenceBarrier +
                ", waitSpinningHelper=" + waitSpinningHelper +
                ", eventHandler=" + eventHandler +
                ", sequence=" + sequence +
                ", exceptionHandler=" + exceptionHandler +
                ", name='" + name + '\'' +
                ", nextSequence=" + nextSequence +
                '}';
    }
}
