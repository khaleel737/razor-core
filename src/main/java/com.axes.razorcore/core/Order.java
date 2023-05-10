package com.axes.razorcore.core;

import com.axes.razorcore.data.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import net.openhft.chronicle.core.io.InvalidMarshallableException;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class Order implements WriteBytesMarshallable, IOrder {

    @Getter
    public long uuid;
    @Getter
    public long orderId;
    @Getter
    public long price;
    @Getter
    public long quantity;
    @Getter
    public Instant timestamp;
    @Getter
    public long size;
    @Getter
    public long filled;
    @Getter
    public long reserveBidPrice;
    @Getter
    public OrderAction action;

    public Order(BytesIn bytes) {
        this.uuid = bytes.readLong();
        this.orderId = bytes.readLong();
        this.price = bytes.readLong();
        this.quantity = bytes.readLong();
        this.timestamp = Instant.ofEpochMilli(bytes.readByte());
        this.size = bytes.readLong();
        this.filled = bytes.readLong();
        this.reserveBidPrice = bytes.readLong();
        this.action = OrderAction.of(bytes.readByte());
    }


    @Override
    public void writeMarshallable(BytesOut<?> bytes) throws IllegalStateException, BufferOverflowException, BufferUnderflowException, IllegalArgumentException, ArithmeticException, InvalidMarshallableException {
        bytes.writeLong(uuid);
        bytes.writeLong(orderId);
        bytes.writeLong(price);
        bytes.writeLong(quantity);
        bytes.writeLong(size);
        bytes.writeLong(filled);
        bytes.writeLong(reserveBidPrice);
        bytes.writeLong(action.getCode());
        bytes.writeLong(timestamp.getNano());
    }


    @Override
    public String toString() {
        return "Order{" +
                "uuid=" + uuid +
                ", orderId=" + orderId +
                ", price=" + price +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                ", size=" + size +
                ", filled=" + filled +
                ", reserveBidPrice=" + reserveBidPrice +
                ", action=" + action +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, action, price, size, filled, reserveBidPrice);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj == null) return false;
        if(!(obj instanceof Order other)) return false;

        return orderId == other.orderId && action == other.action && price == other.price && size == other.size && filled == other.filled && reserveBidPrice == other.reserveBidPrice;
    }

    @Override
    public int stateHash() {
        return hashCode();
    }
}


