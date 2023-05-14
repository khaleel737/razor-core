/*
 * Copyright 2019 Maksim Zheravin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.axes.razorcore.serialization;

import com.axes.razorcore.RazorCoreApi;
import com.axes.razorcore.config.InitialStateConfiguration;
import com.axes.razorcore.cqrs.OrderCommand;
import com.axes.razorcore.journaling.SnapshotDescriptor;
import lombok.AllArgsConstructor;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;

import java.io.IOException;
import java.util.NavigableMap;
import java.util.function.Function;

public interface ISerializationProcessor {

    /**
     * Serialize state into a storage (disk, NAS, etc).<p>
     * Method is threadsafe - called from each module's thread upon receiving serialization command.<p>
     * Method is synchronous - returning true value only when the data was safely stored into independent storage.<p>
     *
     * @param snapshotId  - unique snapshot id
     * @param seq         - sequence of serialization
     * @param timestampNs - timestamp
     * @param type        - module (risk engine or matching engine)
     * @param instanceId  - module instance number (starting from 0 for each module type)
     * @param obj         - serialized data
     * @return true if serialization succeeded, false otherwise
     */
    boolean storeData(long snapshotId,
                      long seq,
                      long timestampNs,
                      SerializedModuleType type,
                      int instanceId,
                      WriteBytesMarshallable obj);

    /**
     * Deserialize state from a storage (disk, NAS, etc).<p>
     * Method is threadsafe - called from each module's thread on creation.<p>
     *
     * @param snapshotId - unique snapshot id
     * @param type       - module (risk engine or matching engine)
     * @param instanceId - module instance number (starting from 0)
     * @param initFunc   - creator lambda function
     * @param <T>        - module implementation class
     * @return constructed object, or throws exception
     */
    <T> T loadData(long snapshotId,
                   SerializedModuleType type,
                   int instanceId,
                   Function<BytesIn, T> initFunc);


    /**
     * Write command into journal
     *
     * @param cmd  - command to write
     * @param dSeq - disruptor sequence
     * @param eob  - if true, journal should commit all previous data synchronously
     * @throws IOException - can throw in case of writing issue (will stop exchange core from responding)
     */
    void writeToJournal(OrderCommand command, long dSeq, boolean eob) throws IOException;


    /**
     * Activate journal
     *
     * @param afterSeq - enable only after specified sequence, for lower sequences no writes to journal
     * @param api      - API reference
     */
    void enableJournaling(long afterSeq, RazorCoreApi api);

    /**
     * get all available snapshots
     *
     * @return sequential map of snapshots (TODO can be a tree)
     */
    NavigableMap<Long, SnapshotDescriptor> findAllSnapshotPoints();

    /**
     * Replay journal
     *
     * @param snapshotId - snapshot id (important for tree history)
     * @param seqFrom    - starting command sequence (exclusive)
     * @param seqTo      - ending command sequence (inclusive)
     * @param api        - API reference
     */
    void replayJournalStep(long snapshotId, long seqFrom, long seqTo, RazorCoreApi api);

    long replayJournalFull(InitialStateConfiguration initialStateConfiguration, RazorCoreApi api);

    void replayJournalFullAndThenEnableJouraling(InitialStateConfiguration initialStateConfiguration, RazorCoreApi exchangeApi);

    boolean checkSnapshotExists(long snapshotId, SerializedModuleType type, int instanceId);

    @AllArgsConstructor
    enum SerializedModuleType {
        RISK_ENGINE("RE"),
        MATCHING_ENGINE_ROUTER("ME");

        final String code;
    }

    static boolean canLoadFromSnapshot(final ISerializationProcessor serializationProcessor,
                                       final InitialStateConfiguration initStateCfg,
                                       final int shardId,
                                       final SerializedModuleType module) {

        if (initStateCfg.fromSnapshot()) {

            final boolean snapshotExists = serializationProcessor.checkSnapshotExists(
                    initStateCfg.getSnapshotId(),
                    module,
                    shardId);

            if (snapshotExists) {
                return true;
            } else {
                // expected to throw if file not found
                if (initStateCfg.isThrowIfSnapshotNotFound()) {
                    throw new IllegalStateException("Snapshot " + initStateCfg.getSnapshotId() + " shardId=" + shardId + " not found for " + module);
                }
            }
        }

        return false;
    }

}
