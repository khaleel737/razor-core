package com.axes.razorcore.config;

import com.axes.razorcore.serialization.DiskSerializationProcessor;
import com.axes.razorcore.serialization.DummySerializationProcessor;
import com.axes.razorcore.serialization.ISerializationProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.function.Function;

@AllArgsConstructor
@Getter
@Builder
@ToString
public class SerializationConfiguration {


    // no serialization
    public static final SerializationConfiguration DEFAULT = SerializationConfiguration.builder()
            .enableJournaling(false)
            .serializationProcessorFactory(cfg -> DummySerializationProcessor.INSTANCE)
            .build();

    // no journaling, only snapshots
    public static final SerializationConfiguration DISK_SNAPSHOT_ONLY = SerializationConfiguration.builder()
            .enableJournaling(false)
            .serializationProcessorFactory(exchangeCfg -> new DiskSerializationProcessor(exchangeCfg, DiskSerializationProcessorConfiguration.createDefaultConfig()))
            .build();

    // snapshots and journaling
    public static final SerializationConfiguration DISK_JOURNALING = SerializationConfiguration.builder()
            .enableJournaling(true)
            .serializationProcessorFactory(exchangeCfg -> new DiskSerializationProcessor(exchangeCfg, DiskSerializationProcessorConfiguration.createDefaultConfig()))
            .build();

    /*
     * Enables journaling.
     * Set to false for analytics instances.
     */
    private final boolean enableJournaling;

    /*
     * Serialization processor implementations
     */
    private final Function<ApplicationConfig, ? extends ISerializationProcessor> serializationProcessorFactory;



}

