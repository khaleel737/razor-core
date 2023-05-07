package com.axes.razorcore.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HazelcastOptions {
    private final String clusterName;
    private final String publicAddress;
    private final String[] addresses;
    private final HazelcastMapConfig[] mapConfigs;
}

@Data
@AllArgsConstructor
public
class HazelcastMapConfig {
    private final String name;
    private final int timeToLiveSeconds;
}

