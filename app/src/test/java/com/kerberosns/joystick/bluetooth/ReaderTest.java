package com.kerberosns.joystick.bluetooth;

import android.os.Handler;
import android.os.Message;

import com.kerberosns.joystick.MainActivity;
import com.kerberosns.joystick.fragments.Joystick;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReaderTest {
    /*
     * The reader should needs to parse the following commands properly.
     *
     * It parses "command":
     * ^
     * command
     * $
     *
     * It parses "command1" and "command2":
     * ^command1$^command2$
     *
     * It parses "command1" and "command2":
     * ^com
     * mand
     * 1$AAA^
     * comma
     * nd2$
     */

    @Mock
    private InputStream mMockInputStream;

    @Mock
    private Handler mMockHandler;

    @Mock
    private Message mMockMessage;

    /*
     * It parses "command" given the following message.
     * AAA^command$
     */
    @Test
    public void parseSingleCommand() {
        // Expectations.
        mMockInputStream = new ByteArrayInputStream("AAA^command$".getBytes());

        when(mMockHandler.obtainMessage(eq(Joystick.Messages.READ), eq("command")))
                .thenReturn(mMockMessage);

        // Exercise.
        Reader reader = new Reader('^', '$', mMockInputStream, mMockHandler);

        try {
            reader.run();
        } catch (NullPointerException e) {
            // Ignore.
        }

        // Verify.
        verify(mMockHandler, times(1))
                .obtainMessage(Joystick.Messages.READ, "command");

        verify(mMockMessage, times(1)).sendToTarget();
    }

    @Test
    public void voidParseMultipleCommands() {
        // Expectations.
        mMockInputStream = new ByteArrayInputStream("AAA^command1$TTT^command2$SSS".getBytes());

        when(mMockHandler.obtainMessage(eq(Joystick.Messages.READ), eq("command1")))
                .thenReturn(mMockMessage);

        when(mMockHandler.obtainMessage(eq(Joystick.Messages.READ), eq("command2")))
                .thenReturn(mMockMessage);

        // Exercise.
        Reader reader = new Reader('^', '$', mMockInputStream, mMockHandler);

        try {
            reader.run();
        } catch (NullPointerException e) {
            // Ignore.
        }

        // Verify.
        verify(mMockHandler, times(1))
                .obtainMessage(Joystick.Messages.READ, "command1");

        verify(mMockHandler, times(1))
                .obtainMessage(Joystick.Messages.READ, "command2");

        verify(mMockMessage, times(2)).sendToTarget();
    }

    @Test
    public void interrupt() throws Exception {
        Reader reader = new Reader('^', '$', mMockInputStream, mMockHandler);
        reader.interrupt();

        reader.start();

        reader.join();
    }
}