package com.axes.razorcore.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccountSnapshot implements Serializable {
    // fields and getters/setters for account balances
     private final String accountId;
        private final long availableBalance;
        private final long pendingBalance;

        public AccountSnapshot(String accountId, long availableBalance, long pendingBalance) {
            this.accountId = accountId;
            this.availableBalance = availableBalance;
            this.pendingBalance = pendingBalance;
        }

        // getters for all fields
}

