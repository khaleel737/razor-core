package com.axes.razorcore.core.Global;

public enum PositionSide {
        /// <summary>
        /// A short position, quantity less than zero (-1)
        /// </summary>
        SHORT(-1),

        /// <summary>
        /// No position, quantity equals zero (0)
        /// </summary>
        NONE(0),

        /// <summary>
        /// A long position, quantity greater than zero (1)
        /// </summary>
        LONG(1);

        private final byte code;

        PositionSide(final int code) {
            this.code = (byte) code;
        }

        public static PositionSide of(final byte code) {
            return switch (code) {
                case -1 -> SHORT;
                case 0 -> NONE;
                case 1 -> LONG;
                default -> throw new IllegalArgumentException("Unknown PositionSide " + code);
            };
        }
}
