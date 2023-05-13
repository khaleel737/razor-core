package com.axes.razorcore.utils;

import com.axes.razorcore.config.RazorCoreWaitStrategy;
import com.lmax.disruptor.*;

import java.lang.reflect.Field;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class WaitSpinningHelper {

    private final SequenceBarrier sequenceBarrier;
    private final Sequencer sequencer;

    private final int spinLimit;
    private final int yieldLimit;

    // blocking mode, using same locking objects that Disruptor operates with
    private final boolean block;
    private final BlockingWaitStrategy blockingDisruptorWaitStrategy;
    private final Lock lock;
    private final Condition processorNotifyCondition;
    // next Disruptor release will have mutex (to avoid allocations)
    // private final Object mutex;

    public <T> WaitSpinningHelper(RingBuffer<T> ringBuffer, SequenceBarrier sequenceBarrier, int spinLimit, RazorCoreWaitStrategy waitStrategy) {
        this.sequenceBarrier = sequenceBarrier;
        this.spinLimit = spinLimit;
        this.sequencer = extractSequencer(ringBuffer);
        this.yieldLimit = waitStrategy.isYield() ? spinLimit / 2 : 0;

        this.block = waitStrategy.isBlock();
        if (block) {
            this.blockingDisruptorWaitStrategy = ReflectionUtils.extractField(AbstractSequencer.class, (AbstractSequencer) sequencer, "waitStrategy");
            this.lock = ReflectionUtils.extractField(BlockingWaitStrategy.class, blockingDisruptorWaitStrategy, "lock");
            this.processorNotifyCondition = ReflectionUtils.extractField(BlockingWaitStrategy.class, blockingDisruptorWaitStrategy, "processorNotifyCondition");
        } else {
            this.blockingDisruptorWaitStrategy = null;
            this.lock = null;
            this.processorNotifyCondition = null;
        }
    }

    public long tryWaitFor(final long seq) throws AlertException, InterruptedException {
        sequenceBarrier.checkAlert();

        long spin = spinLimit;
        long availableSequence;
        while ((availableSequence = sequenceBarrier.getCursor()) < seq && spin > 0) {
            if (spin < yieldLimit && spin > 1) {
                Thread.yield();
            } else if (block) {
/*
                synchronized (mutex) {
                    sequenceBarrier.checkAlert();
                    mutex.wait();
                }
*/
                lock.lock();
                try {
                    sequenceBarrier.checkAlert();
                    // lock only if sequence barrier did not progressed since last check
                    if (availableSequence == sequenceBarrier.getCursor()) {
                        processorNotifyCondition.await();
                    }
                } finally {
                    lock.unlock();
                }
            }

            spin--;
        }

        return (availableSequence < seq)
                ? availableSequence
                : sequencer.getHighestPublishedSequence(seq, availableSequence);
    }

    public void signalAllWhenBlocking() {
        if (block) {
            blockingDisruptorWaitStrategy.signalAllWhenBlocking();
        }
    }

    private static <T> Sequencer extractSequencer(RingBuffer<T> ringBuffer) {
        try {
            final Field f = ReflectionUtils.getField(RingBuffer.class, "sequencer");
            f.setAccessible(true);
            return (Sequencer) f.get(ringBuffer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Can not access Disruptor internals: ", e);
        }
    }
}
