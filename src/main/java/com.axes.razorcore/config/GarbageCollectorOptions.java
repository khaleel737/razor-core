package com.axes.razorcore.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GarbageCollectorOptions {
    private final boolean useG1GC;
    private final int maxGCPauseMillis;
    private final int initiatingHeapOccupancyPercent;
    private final int parallelism;
    private final boolean concurrentMarkEnabled;
}

