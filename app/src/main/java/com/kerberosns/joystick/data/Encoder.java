package com.kerberosns.joystick.data;

/**
 * Encode messages that need to be sent through the bluetooth channel. These messages are commands
 * that the RC car needs to decode. "Move" or "change LED sequence" are examples of commands that
 * the RC car needs to decode. Some of these commands are followed by some data. For example,
 * while sending a command to move, the command contains additional data that tells the RC car how
 * fast to move.
 *
 * There are two commands:
 *  1. Change LED sequence, which does not need additional data.
 *  2. Movement, which needs six additional bits.
 *
 * In order to make the codification and de-codification easy, all commands have the same length,
 * fixed at one byte.
 *
 * Codification:
 *
 * Change LED sequence: 0b00000000
 * Movement           : 0b01[0,1]{6}
 */
public class Encoder {
    public byte changeLedSequence() {
        return 0x00;
    }

    public byte move(byte vertical, byte horizontal) {
        byte _vertical = (byte) ((vertical & 0x07) << 3);
        byte _horizontal = (byte) (horizontal & 0x07);
        return (byte) (0x40 | _vertical | _horizontal);
    }

    public byte setMotorDistribution(int distribution) {
        return (byte) (0x80 | distribution);
    }
}
