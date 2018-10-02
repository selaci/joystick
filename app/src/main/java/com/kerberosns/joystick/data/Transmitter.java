package com.kerberosns.joystick.data;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Sends data over the serial channel.
 */
public class Transmitter {
    private OutputStream mOutputStream;

    /**
     * Constructor.
     * @param outputStream The output stream to use to send data.
     */
    public Transmitter(OutputStream outputStream) {
        mOutputStream = outputStream;
    }

    /**
     * Sends bytes over the output stream.
     * @param array The bytes to be sent.
     * @throws IOException Can throw this exception.
     */
    public void send(int[] array) throws IOException {
        for (int number : array) {
            mOutputStream.write(number);
        }
    }
}
