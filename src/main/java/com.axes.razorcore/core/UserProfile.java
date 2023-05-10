package com.axes.razorcore.core;

import com.axes.razorcore.utils.SerializationUtils;
import com.axes.razorcore.utils.StateHash;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import net.openhft.chronicle.core.io.InvalidMarshallableException;
import org.eclipse.collections.impl.map.mutable.primitive.IntLongHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;


public class UserProfile implements WriteBytesMarshallable, StateHash {

    public final long uuid;
    public final IntObjectHashMap<SymbolPosition> userPositions;

public long adjustmentsCounter;

public final IntLongHashMap accounts;

public UserStatus userStatus;

    public UserProfile(long uuid, UserStatus userStatus) {
        this.uuid = uuid;
        this.userStatus = userStatus;
        this.adjustmentsCounter = 0L;
        this.userPositions = new IntObjectHashMap<>();
        this.accounts = new IntLongHashMap();
    }

    public UserProfile(BytesIn bytesIn) {
        this.uuid = bytesIn.readLong();
        this.userStatus = UserStatus.of(bytesIn.readByte());
        this.adjustmentsCounter = bytesIn.readLong();
        this.userPositions = SerializationUtils.readIntHashMap(bytesIn, bytes -> new SymbolPosition(uuid, bytes));
        this.accounts = SerializationUtils.readIntLongHashMap(bytesIn);
        this.userStatus = UserStatus.of(bytesIn.readByte());
    }

    @Override
    public int stateHash() {
        return 0;
    }

    @Override
    public void writeMarshallable(BytesOut<?> bytes) throws IllegalStateException, BufferOverflowException, BufferUnderflowException, IllegalArgumentException, ArithmeticException, InvalidMarshallableException {

    }
}
