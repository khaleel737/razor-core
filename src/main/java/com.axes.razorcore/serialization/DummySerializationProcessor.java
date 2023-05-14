package com.axes.razorcore.serialization;

import com.axes.razorcore.RazorCoreApi;
import com.axes.razorcore.config.InitialStateConfiguration;
import com.axes.razorcore.cqrs.OrderCommand;
import com.axes.razorcore.journaling.SnapshotDescriptor;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;

import java.util.NavigableMap;
import java.util.function.Function;

public class DummySerializationProcessor implements ISerializationProcessor {

    public static final DummySerializationProcessor INSTANCE = new DummySerializationProcessor();

    @Override
    public boolean storeData(long snapshotId, long seq, long timestampNs, SerializedModuleType type, int instanceId, WriteBytesMarshallable obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T loadData(long snapshotId, SerializedModuleType type, int instanceId, Function<BytesIn, T> initFunc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeToJournal(OrderCommand cmd, long dSeq, boolean eob) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableJournaling(long afterSeq, RazorCoreApi api) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableMap<Long, SnapshotDescriptor> findAllSnapshotPoints() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replayJournalStep(long snapshotId, long seqFrom, long seqTo, RazorCoreApi exchangeApi) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long replayJournalFull(InitialStateConfiguration initialStateConfiguration, RazorCoreApi exchangeApi) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replayJournalFullAndThenEnableJouraling(InitialStateConfiguration initialStateConfiguration, RazorCoreApi exchangeApi) {
        // do nothing
    }

    @Override
    public boolean checkSnapshotExists(long snapshotId, SerializedModuleType type, int instanceId) {
        return false;
    }
}
