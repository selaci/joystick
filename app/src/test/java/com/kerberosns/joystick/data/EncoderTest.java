package com.kerberosns.joystick.data;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EncoderTest {
    private Encoder mEncoder;
    @Before
    public void setUp() {
        mEncoder = new Encoder();
    }
    @Test
    public void changeLedSequence() {
        byte encoded = mEncoder.changeLedSequence();
        assertEquals(0b00000000, encoded);
    }

    @Test
    public void moveTest_3_and_0() {
        byte encoded = mEncoder.move((byte) 3, (byte) 0);
        assertEquals(0b01011000, encoded);
    }

    @Test
    public void moveTest_7_and_0() {
        byte encoded = mEncoder.move((byte) 7, (byte) 0);
        assertEquals(0b01111000, encoded);
    }

    @Test
    public void moveTest_0_and_2() {
        byte encoded = mEncoder.move((byte) 0, (byte) 2);
        assertEquals(0b01000010, encoded);
    }

    @Test
    public void setMotorDistribution_0() {
        byte encoded = mEncoder.setMotorDistribution(0);
        assertEquals((byte) 0x80, encoded);
    }

    @Test
    public void setMotorDistribution_50() {
        byte encoded = mEncoder.setMotorDistribution(50);
        assertEquals((byte) 0xB2, encoded);
    }

    @Test
    public void setMotorDistribution_100() {
        byte encoded = mEncoder.setMotorDistribution(100);
        assertEquals((byte) 0xE4, encoded);
    }
}