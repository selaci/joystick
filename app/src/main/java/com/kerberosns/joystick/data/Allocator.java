package com.kerberosns.joystick.data;

/**
 * This maps X and Y positions to power numbers.
 *
 * For example, assuming that the vertical joystick can be split between seven positions, if the
 * vertical joystick is on the very to of the vertical axis then, this should return 0. On the other
 * hand if it's at the very bottom then, this returns six.
 */
public class Allocator {
    /**
     * The size of the block the axis is divided by. For example of we had an axis with length one
     * hundred and the number of divisions is four then, mBlock will be twenty-five.
     */
    private final int mBlock;

    /** The minor value in the axis. */
    private final int mMinor;

    /**
     * Initialise the allocator.
     * @param length The height of the axis. If the axis is the horizontal one then, this is
     *               equivalent to the width.
     * @param divisions The number of divisions in the axis. For example seven.
     * @param minor The minor value in the axis. For example, for the vertical axis this is the top,
     *            however for the horizontal axis, this is the left.
     */
    public Allocator(int length, int divisions, int minor) {
        mBlock = length / divisions;
        mMinor = minor;
    }

    /**
     * Map the current position to the number of the block.
     * @param pos The current X or Y position.
     * @return A number that fits in a byte.
     */
    public byte allocate(int pos) {
        return (byte) ((pos - mMinor) / mBlock);
    }
}
