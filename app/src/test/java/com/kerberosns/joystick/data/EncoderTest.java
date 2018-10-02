package com.kerberosns.joystick.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class EncoderTest {
    @Test
    public void changeLedSequence() {
        Encoder encoder = new Encoder();
        int[] array = encoder.changeLedSequence();

        assertEquals(0b01011110, array[0]);
        assertEquals(0b00010000, array[1]);
        assertEquals(0b00100100, array[2]);
    }

    @Test
    public void moveTest421and724() {
        Encoder encoder = new Encoder();
        int[] actual = encoder.move(421, 724);

        assertEquals(0b01011110, actual[0]);
        assertEquals(0b00100110, actual[1]);
        assertEquals(0b10010110, actual[2]);
        assertEquals(0b11010100, actual[3]);
        assertEquals(0b00100100, actual[4]);
    }

    @Test
    public void moveTestWithMarkers1001and3() {
        Encoder encoder = new Encoder('a', 'z');
        // a = 01100001
        // z = 01111010
        int[] actual = encoder.move(1001, 3);

        assertEquals(0b01100001, actual[0]);
        assertEquals(0b00101111, actual[1]);
        assertEquals(0b10100100, actual[2]);
        assertEquals(0b00000011, actual[3]);
        assertEquals(0b01111010, actual[4]);
    }
}