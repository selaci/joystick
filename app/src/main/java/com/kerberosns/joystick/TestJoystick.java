package com.kerberosns.joystick;
import android.util.Log;

/**
 * Provides a joystick to the user, but no command is sent. This is for testing purposes.
 */
public class TestJoystick extends Joystick {
    @Override
    protected void write(byte encoded) {
        int vertical = encoded & 0x07;
        int horizontal = (encoded & 0x38) >>> 3;

        String message = "Write: " + Integer.toBinaryString(encoded)
                + " Vertical: " + vertical + ". Horizontal: " + horizontal;

        Log.d(MainActivity.TAG, message);
    }
}
