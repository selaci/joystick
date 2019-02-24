package com.kerberosns.joystick;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Provides a joystick to the user, by which commands can be send to move or change the LED
 * sequence.
 */
public class RealJoystick extends Joystick {
    /** This is the bluetooth device this joystick connects to. */
    private BluetoothDevice mBluetoothDevice;

    /** The bluetooth socket that lets you communicate with the device. */
    private BluetoothSocket mSocket;

    /** The bluetooth socket's output stream. */
    private OutputStream mOutputStream;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mBluetoothDevice = savedInstanceState.getParcelable(MainActivity.BLUETOOTH);
        } else {
            Intent intent = getIntent();
            mBluetoothDevice = intent.getParcelableExtra(MainActivity.BLUETOOTH);

            if (mBluetoothDevice == null) {
                showMessageToUser(R.string.bluetooth_not_found);
                finish();
            }
        }
    }

    class ConnectToDeviceTask extends AsyncTask<BluetoothDevice, Void, BluetoothSocket> {
        private ProgressDialog mmDialog;

        private final Context mmContext;

        ConnectToDeviceTask(Context context) {
            mmContext = context;
        }

        @Override
        protected void onPreExecute() {
            mmDialog = new ProgressDialog(mmContext);
            mmDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mmDialog.setMessage("Connecting");
            mmDialog.setIndeterminate(true);
            mmDialog.show();
        }

        @Override
        protected BluetoothSocket doInBackground(BluetoothDevice... devices) {
            UUID uuid = devices[0].getUuids()[0].getUuid();
            BluetoothSocket socket = null;
            try {
                socket = devices[0].createRfcommSocketToServiceRecord(uuid);
                socket.connect();
            } catch (IOException e) {
                // Ignore this exception, but post execute will deal with this scenario.
                Log.i(MainActivity.TAG, "exception");
            }

            return socket;
        }

        @Override
        protected void onPostExecute(BluetoothSocket socket) {
            mmDialog.dismiss();

            if (socket.isConnected()) {
                mSocket = socket;
                try {
                    mOutputStream = mSocket.getOutputStream();

                    setMotorDistribution();
                } catch (IOException e) {
                    showMessageToUser(R.string.bluetooth_not_connected);
                    closeSocket();
                    finish();
                }
            } else {
                showMessageToUser(R.string.bluetooth_not_connected);
                finish();
            }
        }
    }

    private void closeSocket() {
        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            String message = getResources().getString(R.string.bluetooth_close_socket);
            Log.e(MainActivity.TAG, message);
        }
    }

    /**
     * Toast a message to the user for a long time of seconds.
     * @param resource The resource ID that identifies the message.
     */
    private void showMessageToUser(int resource) {
        String message = getResources().getString(resource);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void write(byte encoded) {
       try {
           mOutputStream.write(encoded);
        } catch (IOException e) {
           showMessageToUser(R.string.bluetooth_not_connected);
           closeSocket();
           finish();
        } catch (NullPointerException e) {
           showMessageToUser(R.string.bluetooth_not_connected);
           closeSocket();
           finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(MainActivity.BLUETOOTH, mBluetoothDevice);
    }

    @Override
    public void onResume() {
        super.onResume();
        new ConnectToDeviceTask(this).execute(mBluetoothDevice);
    }
    @Override
    public void onPause() {
        super.onPause();
        closeSocket();
    }
}
