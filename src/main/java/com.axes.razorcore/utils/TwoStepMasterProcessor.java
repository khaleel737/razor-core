package com.axes.razorcore.utils;

import com.axes.razorcore.config.RazorCoreWaitStrategy;
import com.axes.razorcore.cqrs.OrderCommandType;
import com.axes.razorcore.cqrs.OrderCommand;
import com.lmax.disruptor.*;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

public class TwoStepMasterProcessor implements EventProcessor {

    private static final int IDLE = 0;
    private static final int HALTED = IDLE + 1;

    private static final int RUNNING = HALTED + 1;

    private static final int MASTER_SPINT_LIMIT = 5000;

    private final AtomicInteger running = new AtomicInteger(IDLE);

    private final DataProvider<OrderCommand> dataProvider;

    private final SequenceBarrier sequenceBarrier;

    private final WaitSpinningHelper waitSpinningHelper;

    private final SimpleEventHandler simpleEventsHandler;

    private final ExceptionHandler<OrderCommand> exceptionHandler;

    private final String name;

    private final Sequence sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);

    @Setter
    private TwoStepSlaveProcessor twoStepSlaveProcessor;

    public TwoStepMasterProcessor(RingBuffer<OrderCommand> ringBuffer,
                                  SequenceBarrier sequenceBarrier,
                                  SimpleEventHandler simpleEventsHandler,
                                  ExceptionHandler<OrderCommand> exceptionHandler,
                                  RazorCoreWaitStrategy razorCoreWaitStrategy,
                                  String name) {
        this.dataProvider = ringBuffer;
        this.sequenceBarrier = sequenceBarrier;
        this.waitSpinningHelper = new WaitSpinningHelper(ringBuffer, sequenceBarrier, MASTER_SPINT_LIMIT, razorCoreWaitStrategy);
        this.simpleEventsHandler = simpleEventsHandler;
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
    if(running.compareAndSet(IDLE, RUNNING)) {
        try {
            if(running.get() == RUNNING) {
                processEvents();
            }
        } finally {
            running.set(IDLE);
        }
    }
    }

    private void processEvents() {
        Thread.currentThread().setName("Thread-" + name);

        long nextSequence = sequence.get() + 1L;

        long currentSequenceGroup = 0;

        while (!twoStepSlaveProcessor.isRunning()) {
            Thread.yield();
        }
        while (true) {
            OrderCommand command = null;
            try {
                final long availableSequence = waitSpinningHelper.tryWaitFor(nextSequence);

                if(nextSequence <= availableSequence) {
                    while (nextSequence <= availableSequence) {
                        command = dataProvider.get(nextSequence);

                        if(command.eventsGroup != currentSequenceGroup) {
                            publishProgressAndTriggerSlaveProcessor(nextSequence);
                            currentSequenceGroup = command.eventsGroup;
                        }

                        boolean forcedPublish = simpleEventsHandler.onEvent(nextSequence, command);
                        nextSequence++;

                        if(forcedPublish) {
                            sequence.set(nextSequence - 1);
                            waitSpinningHelper.signalAllWhenBlocking();
                        }

                        if(command.commandType == OrderCommandType.SHUTDOWN_SIGNAL) {
                            publishProgressAndTriggerSlaveProcessor(nextSequence);
                        }
                    }

                    sequence.set(availableSequence);
                    waitSpinningHelper.signalAllWhenBlocking();
                }
            } catch (final AlertException exception) {
                if(running.get() != RUNNING) {
                    break;
                }

            } catch (final Throwable throwable) {
                exceptionHandler.handleEventException(throwable, nextSequence, command);
                sequence.set(nextSequence);
                waitSpinningHelper.signalAllWhenBlocking();
                nextSequence++;
            }
        }
    }

    private void publishProgressAndTriggerSlaveProcessor(final long nextSequence) {
        sequence.set(nextSequence - 1);
        waitSpinningHelper.signalAllWhenBlocking();
        twoStepSlaveProcessor.handlingCycle(nextSequence);
    }

    @Override
    public String toString() {
        return "TwoStepMasterProcessor{" +
                "running=" + running +
                ", dataProvider=" + dataProvider +
                ", sequenceBarrier=" + sequenceBarrier +
                ", waitSpinningHelper=" + waitSpinningHelper +
                ", simpleEventsHandler=" + simpleEventsHandler +
                ", exceptionHandler=" + exceptionHandler +
                ", name='" + name + '\'' +
                ", sequence=" + sequence +
                ", twoStepSlaveProcessor=" + twoStepSlaveProcessor +
                '}';
    }
}
