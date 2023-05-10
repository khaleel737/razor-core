package com.axes.razorcore.order;

import com.axes.razorcore.data.OrderCommand;

import java.io.Serializable;

    public class OrderRejectEvent implements Serializable {
        private String accountId;
        private OrderCommand orderCommand;
        private String reason;

        public OrderRejectEvent(String accountId, OrderCommand orderCommand, String reason) {
            this.accountId = accountId;
            this.orderCommand = orderCommand;
            this.reason = reason;
        }

        public String getAccountId() {
            return accountId;
        }

        public OrderCommand getOrderCommand() {
            return orderCommand;
        }

        public String getReason() {
            return reason;
        }
}
