package com.axes.razorcore.journaling;

import com.axes.razorcore.data.AccountSnapshot;
import com.axes.razorcore.data.OrderBookSnapshot;
import net.openhft.chronicle.core.io.InvalidMarshallableException;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;

import java.io.IOException;
import java.util.Objects;

public class Journal {
    private final String accountSnapshotPath;
    private final String orderBookSnapshotPath;
    private final String orderCommandPath;
    private final ChronicleQueue chronicleQueue;

    public Journal(String basePath, String accountSnapshotPath, String orderBookSnapshotPath, String orderCommandPath) throws IOException {
        this.accountSnapshotPath = accountSnapshotPath;
        this.orderBookSnapshotPath = orderBookSnapshotPath;
        this.orderCommandPath = orderCommandPath;
        this.chronicleQueue = ChronicleQueue.singleBuilder(basePath).build();
    }

    public void writeAccountSnapshot(AccountSnapshot accountSnapshot) throws Exception {
        try (DocumentContext dc = chronicleQueue.acquireAppender().writingDocument()) {
            dc.wire().write("accountSnapshot").marshallable(m -> m.writeDocument(accountSnapshot));
        } catch (NullPointerException | InvalidMarshallableException e) {
            throw new Exception(e);
        }
    }

    public void writeOrderBookSnapshot(OrderBookSnapshot orderBookSnapshot) throws Exception {
        try (DocumentContext dc = chronicleQueue.acquireAppender().writingDocument()) {
            dc.wire().write("orderBookSnapshot").marshallable(wire -> wire.writeDocument(orderBookSnapshot));
        } catch (NullPointerException | InvalidMarshallableException e) {
            throw new Exception(e);
        }
    }


    public void replayEvents() {
        ExcerptTailer tailer = chronicleQueue.createTailer();
        while (tailer.readDocument(reader -> {
            try {
                String eventName = reader.readText();
                switch (Objects.requireNonNull(eventName)) {
                    case "orderCommand":
                        // TODO: replay order command event
                        break;
                    case "orderFillEvent":
                        // TODO: replay order fill event
                        break;
                    case "orderRejectEvent":
                        // TODO: replay order reject event
                        break;
                    case "accountSnapshot":
                        AccountSnapshot accountSnapshot = new AccountSnapshot();
                        accountSnapshot.readMarshallable(reader);
                        // TODO: restore account snapshot
                        break;
                    case "orderBookSnapshot":
                        OrderBookSnapshot orderBookSnapshot = new OrderBookSnapshot();
                        orderBookSnapshot.readMarshallable(reader);
                        // TODO: restore order book snapshot
                        break;
                }
            } catch (NullPointerException e) {
                try {
                    throw new Exception(e);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        })) {}
    }




    public void close() throws IOException {
        chronicleQueue.close();
    }
}
