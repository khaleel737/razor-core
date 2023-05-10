package com.axes.razorcore.data;

import lombok.Data;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.SelfDescribingMarshallable;

import java.io.Serializable;

@Data
public class AccountSnapshot extends SelfDescribingMarshallable implements Serializable, Marshallable {
    // fields and getters/setters for account balances
    private String accountId;
    private long availableBalance;
    private long pendingBalance;

    public AccountSnapshot() {
        // default constructor
    }

    public AccountSnapshot(String accountId, long availableBalance, long pendingBalance) {
        this.accountId = accountId;
        this.availableBalance = availableBalance;
        this.pendingBalance = pendingBalance;
    }


    // getters/setters for all fields
}
