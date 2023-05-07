package com.axes.razorcore.journaling;

import com.axes.razorcore.data.AccountSnapshot;
import com.axes.razorcore.data.OrderBookSnapshot;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;

import java.io.IOException;

public class Journal {
    private final String accountSnapshotPath;
    private final String orderBookSnapshotPath;
    private final ChronicleQueue chronicleQueue;

    public Journal(String basePath, String accountSnapshotPath, String orderBookSnapshotPath) throws IOException {
        this.accountSnapshotPath = accountSnapshotPath;
        this.orderBookSnapshotPath = orderBookSnapshotPath;
        this.chronicleQueue = ChronicleQueue.singleBuilder(basePath).build();
    }

    public void writeAccountSnapshot(AccountSnapshot accountSnapshot) throws Exception {
        try (DocumentContext dc = chronicleQueue.acquireAppender().writingDocument()) {
            dc.wire().write("accountSnapshot").marshallable(m -> m.writeObject(accountSnapshot));
        } catch (NullPointerException e) {
            throw new Exception(e);
        }
    }

    public void writeOrderBookSnapshot(OrderBookSnapshot orderBookSnapshot) throws Exception {
        try (DocumentContext dc = chronicleQueue.acquireAppender().writingDocument()) {
            dc.wire().write("orderBookSnapshot").marshallable(m -> m.writeObject(orderBookSnapshot));
        } catch (NullPointerException e) {
            throw new Exception(e);
        }
    }

    public void replayEvents() {
        ExcerptTailer tailer = chronicleQueue.createTailer();
        while (tailer.readDocument(d -> {
            String eventName = d.readText();
            switch (eventName) {
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
                    AccountSnapshot accountSnapshot = d.read().marshallable(m -> m.readObject(AccountSnapshot.class));
                    // TODO: restore account snapshot
                    break;
                case "orderBookSnapshot":
                    OrderBookSnapshot orderBookSnapshot = d.wire().read().marshallable(m -> m.readObject(OrderBookSnapshot.class));
                    // TODO: restore order book snapshot
                    break;
            }
        })) {}
    }

    public void close() throws IOException {
        chronicleQueue.close();
    }
}
