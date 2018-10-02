package com.kerberosns.joystick.data;

/**
 * Encode commands into messages.
 *
 * Message formant and contents depend on each type of command and data that may follow the command.
 *
 * At this moment there are only two commands. They are "Changing LED sequence" and "Movement".
 * Changing LED sequence does not require any additional data, however "move" does. Move
 * requires two additional numbers that range between 0 and 1023.
 *
 * Additionally each command is prepended by a character, called start marker, and suffixed by
 * another character called end marker.
 *
 * This class tries to safe as much space as possible, so it does some bit operations in order to
 * reduce the amount of bytes to transmit.
 *
 * Next examples assume that start marker is "^" and end marker is "$".
 *
 * "^" is 94 in the ASCII table, which is "01011110" in binary in one byte.
 * "$" is 36 in the ASCII table, which is "00100100" in binary in one byte.
 *
 * Change LED sequence command is coded as number 16, which is "00010000" in binary in one byte.
 * Movement command is coded as number 32, which is "00100000" in binary in one byte.
 *
 * Change LED sequence:
 *
 * 01011110 00010000 00100100
 *
 * This is three bytes and the reason why the byte in the middle has the last 4 bits set to zero is
 * in order to not interfere with possible data that follow the command. Next example shows what I
 * mean.
 *
 * Move:
 *
 * This command requires two numbers that range from 0 to 1023. This can be represented by 10 bits.
 *
 * In this example I assume that the coordinate X and Y are 421 and 724.
 *
 * 421 is "01 1010 0101"
 * 724 is "10 1101 0100"
 *
 * The command follows the format: "^ move X Y $". I add spaces only to make the code more
 * readable.
 *
 * 01011110 00100110 10010110 11010100 00100100
 */
public class Encoder {
    /**
     * The character that signals that a new message starts.
     */
    private char mStartMarker;

    /**
     * The character that signals that a message ends.
     */
    private char mEndMarker;

    /**
     * Constructor.
     * @param startMarker The character that signals that a new message starts.
     * @param endMarker The character that signals that a message ends.
     */
    public Encoder(char startMarker, char endMarker) {
        mStartMarker = startMarker;
        mEndMarker = endMarker;
    }

    /**
     * Constructor with defaults.
     * Start marker is "^".
     * End marker is "$".
     */
    public Encoder() {
        this('^', '$');
    }

    /**
     * Encode the command to change the LED sequence in an array of integers. The reason this uses
     * integers instead of bytes is because bytes are signed in java and range from -128 to 127. So,
     * @return An array of integers.
     */
    public int[] changeLedSequence() {
        return new int[] {mStartMarker, 16, mEndMarker};
    }

    /**
     * Encode the move command and the coordinates X and Y.
     * The reason this uses
     * integers instead of bytes is because bytes are signed in java and range from -128 to 127. So,
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return An array of integers.
     */
    public int[] move(int x, int y) {
        return new int[] {
                mStartMarker,
                32 | (x >>> 6) & 0xF,
                (x << 2) & 0xFC | (y >>> 8) & 0x3,
                y & 0xFF,
                mEndMarker};
    }
}
