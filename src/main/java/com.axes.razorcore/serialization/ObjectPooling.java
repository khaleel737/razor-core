package com.axes.razorcore.serialization;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

public class ObjectPooling<T> {

    private final LinkedBlockingQueue<T> objectPool;

    public ObjectPooling(int poolSize, Supplier<T> objectSupplier) {
        this.objectPool = new LinkedBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            objectPool.offer(objectSupplier.get());
        }
    }

    public T acquire() {
        try {
            return objectPool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void release(T object) {
        objectPool.offer(object);
    }

    public int getSize() {
        return objectPool.size();
    }
}

