package com.axes.razorcore.exception;

import com.lmax.disruptor.ExceptionHandler;
import lombok.RequiredArgsConstructor;

import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class DisruptorException<T> implements ExceptionHandler<T> {

    public final String name;
    public final BiConsumer<Throwable, Long> onException;

    @Override
    public void handleEventException(Throwable ex, long sequence, T event) {
//        log.debug("Disruptor '{}' seq={} caught exception: {}", name, sequence, event, ex);
        onException.accept(ex, sequence);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
//        log.debug("Disruptor '{}' startup exception: {}", name, ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
//        log.debug("Disruptor '{}' shutdown exception: {}", name, ex);
    }
}
