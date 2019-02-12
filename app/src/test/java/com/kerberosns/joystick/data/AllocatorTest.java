package com.kerberosns.joystick.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class AllocatorTest {

    @Test
    public void allocate() {
        /* With these settings, each block has a size of 142 approximately. */
        Allocator allocator = new Allocator(1000, 7, 200);

        assertEquals(0, allocator.allocate(220));
        assertEquals(1, allocator.allocate(343));
        assertEquals(2, allocator.allocate(500));
        assertEquals(3, allocator.allocate(720));
        assertEquals(4, allocator.allocate(800));
        assertEquals(5, allocator.allocate(950));
        assertEquals(6, allocator.allocate(1100));
    }
}