package com.kerberosns.joystick.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class EncoderTest {
    @Test
    public void changeLedSequence() {
        Encoder encoder = new Encoder();
        byte encoded = encoder.changeLedSequence();

        assertEquals(0b00000000, encoded);
    }

    @Test
    public void moveTest_3_and_0() {
        Encoder encoder = new Encoder();
        byte encoded = encoder.move(3, 0);

        assertEquals(0b01011000, encoded);
    }

    @Test
    public void moveTest_7_and_0() {
        Encoder encoder = new Encoder();
        byte encoded = encoder.move(7, 0);

        assertEquals(0b01111000, encoded);
    }

    @Test
    public void moveTest_0_and_2() {
        Encoder encoder = new Encoder();
        byte encoded = encoder.move(0, 2);

        assertEquals(0b01000010, encoded);
    }
}