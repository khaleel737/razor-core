package com.axes.razorcore.service;

import com.axes.razorcore.core.SymbolSpecification;
import com.axes.razorcore.utils.HashingUtils;
import com.axes.razorcore.utils.SerializationUtils;
import com.axes.razorcore.utils.StateHash;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.Objects;

public class SymbolSpecificationProvider implements WriteBytesMarshallable, StateHash {

    private final IntObjectHashMap<SymbolSpecification> symbolSpecifications;

    public SymbolSpecificationProvider() {
        this.symbolSpecifications = new IntObjectHashMap<>();
    }

    public SymbolSpecificationProvider(BytesIn bytes) {
        this.symbolSpecifications = SerializationUtils.readIntHashMap(bytes, SymbolSpecification::new);
    }

    public boolean addSymbol(final SymbolSpecification symbolSpecification) {
        if (getSymbolSpecification(symbolSpecification.symbolId) != null) {
            return false; // CommandResultCode.SYMBOL_MGMT_SYMBOL_ALREADY_EXISTS;
        } else {
            registerSymbol(symbolSpecification.symbolId, symbolSpecification);
            return true;
        }
    }

    public SymbolSpecification getSymbolSpecification(int symbol) {
        return symbolSpecifications.get(symbol);
    }

    public void registerSymbol(int symbol, SymbolSpecification spec) {
        symbolSpecifications.put(symbol, spec);
    }

    public void reset() {
        symbolSpecifications.clear();
    }

    @Override
    public void writeMarshallable(BytesOut bytes) {
        // write symbolSpecs
        SerializationUtils.marshallIntHashMap(symbolSpecifications, bytes);
    }

    @Override
    public int stateHash() {
        return Objects.hash(HashingUtils.stateHash(symbolSpecifications));
    }

}
