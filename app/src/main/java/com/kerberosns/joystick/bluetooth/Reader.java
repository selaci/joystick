package com.kerberosns.joystick.bluetooth;

import android.os.Handler;
import android.util.Log;

import com.kerberosns.joystick.MainActivity;
import com.kerberosns.joystick.fragments.Joystick;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Keeps reading from a bluetooth channel until the thread gets stopped or cancelled.
 *
 * The messages are sent back to the calling activity through a message handler.
 */
public class Reader extends Thread {
    private final char mStartMarker;
    private final char mEndMarker;

    private final BufferedReader mBufferedReader;

    private final Handler mHandler;

    private boolean keepGoing = true;

    public Reader(char startMarker, char endMarker, InputStream inputStream, Handler handler) {
        mStartMarker = startMarker;
        mEndMarker = endMarker;
        mBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        mHandler = handler;
    }

    @Override
    public void run() {
        while(keepGoing) {
            try {
                String line = mBufferedReader.readLine();
                parse(line);
            } catch (IOException e) {
                if (keepGoing) {
                    Log.d(MainActivity.TAG, e.toString());
                }
            }
        }
    }

    private void parse(String line) {
       int index = line.indexOf(mStartMarker);
       if (index == -1) {
           return;
       }

       if (line.length() <= index) {
           return;
       }
       String remaining = line.substring(index + 1);

       index = remaining.indexOf(mEndMarker);
       if (index == -1) {
           return;
       }

       String command = remaining.substring(0, index);
       add(command);

       // If there are more commands in this line:
       if (remaining.length() - 1 > index) {
           if (remaining.length() <= index) {
               return;
           }
           parse(remaining.substring(index + 1));
       }
    }

    private void add(String command) {
        mHandler.obtainMessage(Joystick.Messages.READ, command).sendToTarget();
        Log.d(MainActivity.TAG, "Received command: " + command);
    }

    @Override
    public void interrupt() {
        try {
            keepGoing = false;
            mBufferedReader.close();
        } catch (IOException e) {
            Log.d(MainActivity.TAG, e.toString());
        }
    }
}
