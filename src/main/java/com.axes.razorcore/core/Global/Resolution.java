package com.axes.razorcore.core.Global;

public enum Resolution {
        /// Tick Resolution (0)
        TICK(0),
        /// Second Resolution (1)
        SECOND(1),
        /// Minute Resolution (2)
        MINUTE(2),
        /// Hour Resolution (3)
        HOUR(3),
        /// Daily Resolution (4)
        DAILY(4);

        private final byte code;

        Resolution(final int code) {
            this.code = (byte) code;
        }

        public static Resolution of(final byte code) {
            return switch (code) {
                case 0 -> TICK;
                case 1 -> SECOND;
                case 2 -> MINUTE;
                case 3 -> HOUR;
                case 4 -> DAILY;
                default -> throw new IllegalArgumentException("Unknown Resolution " + code);
            };
        }
}
