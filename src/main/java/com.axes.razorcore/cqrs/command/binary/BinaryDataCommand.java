package com.axes.razorcore.cqrs.command.binary;

import net.openhft.chronicle.bytes.WriteBytesMarshallable;

public interface BinaryDataCommand extends WriteBytesMarshallable {

    int getBinaryCommandTypeCode();

}
