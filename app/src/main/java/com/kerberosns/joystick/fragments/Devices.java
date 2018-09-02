package com.kerberosns.joystick.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.kerberosns.joystick.Adapter;
import com.kerberosns.joystick.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;

public class Devices extends Fragment implements Adapter.OnDeviceClickListener {
    /**
     * The button to discover devices once pressed.
     */
    private Button mDiscoverButton;

    /**
     * The request code for creating a new bond with a bluetooth device.
     */
    private static final int REQUEST_BOND = 0;

    /**
     * The callback methods the activity hosting this fragment must implement.
     */
    public interface Listener {
        /**
         * When the bluetooth is not enabled and the user has to enable it.
         */
        void onRequestEnableBluetooth();

        /**
         * When the user selects a device from a list of devices.
         * @param device The device the user has selected.
         */
        void onBluetoothDeviceSelected(BluetoothDevice device);

        /**
         * When the application has to finish as a result of a non-recoverable exception.
         */
        void onFinish();
    }

    /**
     * The messages that can be delivered to this fragment through a handler.
     */
    interface Messages {
        /**
         * A message to cancel the current discovery.
         */
        int END_DISCOVERY = 0;
    }

    /**
     * A class to signal that the device does not support bluetooth
     */
    private class NotSupportedException extends RuntimeException {}

    /**
     * A class to signal that the device has not the bluetooth enabled.
     */
    private class NotEnabledException extends RuntimeException {}

    /**
     * A listener to send callback calls to the activity that hosts this fragment.
     */
    private Listener mListener;

    @Override
    public void onClick(BluetoothDevice device) {
        // The device is bounded already.
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            endDiscovery();
            mListener.onBluetoothDeviceSelected(device);
        } else {
            createBond(device);
        }
    }

    /**
     * Initiate a dialog for the user to bound the device.
     * @param device The bluetooth device to be paired with.
     */
    private void createBond(BluetoothDevice device) {
        String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
        Intent intent = new Intent(ACTION_PAIRING_REQUEST);
        String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
        intent.putExtra(EXTRA_DEVICE, device);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BOND && resultCode == RESULT_OK) {
            // create a new broadcast receiver.
        }
    }

    /**
     * Factory pattern creation.
     * @return An instance of this fragment.
     */
    public static Devices newInstance() {
        return new Devices();
    }

    /**
     * A handler to deliver messages to this fragment.
     */
    private static class MyHandler extends Handler {
        private WeakReference<Devices> mDevices;

        MyHandler(Devices devices) {
            mDevices = new WeakReference<>(devices);
        }

        @Override
        public void handleMessage(Message message) {
            Devices devices = mDevices.get();
            switch(message.what) {
                case Messages.END_DISCOVERY:
                    devices.endDiscovery();
            }
        }
    }

    private RecyclerView.Adapter mAdapter;

    private List<BluetoothDevice> mDevices = new ArrayList<>();

    /**
     * Bluetooth broadcaster receiver.
     *
     * It can receive two types of messages "ACTION_FOUND" and "ACTION_NAME_CHANGED". I have
     * actually noticed that most of the devices that are discovered do not contain a name at the
     * time of discover. The code below discards devices that are discovered if they don't have a
     * name. Most of the times, the name comes after a few seconds, which is the reason why this
     * receiver also accepts "ACTION_NAME_CHANGED" events.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device;
                    device = intent.getParcelableExtra(
                            EXTRA_DEVICE);

                    if (device.getName() != null) {
                        addBluetoothDevice(device);
                    }
                } else if (action.equals(BluetoothDevice.ACTION_NAME_CHANGED)) {
                    BluetoothDevice device = intent
                            .getParcelableExtra(EXTRA_DEVICE);

                    addBluetoothDevice(device);
                } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void addBluetoothDevice(BluetoothDevice device) {
        if (!inTheList(device)) {
            mDevices.add(device);
            mAdapter.notifyDataSetChanged();
        }
    }

    private boolean inTheList(BluetoothDevice device) {
        boolean found = false;

        for(BluetoothDevice _device : mDevices) {
            if (_device.getName().equals(device.getName())) {
                found = true;
                break;
            }
        }

        return found;
    }

    private Handler mHandler;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothHeadset mBluetoothHeadset;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Devices.Listener) {
            mListener = (Listener) context;
        } else {
            String message = getResources().getString(R.string.implementation_exception);
            throw new ClassCastException(message);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new MyHandler(this);

        // Register bluetooth broadcast message for bonding devices.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);
    }

    @SuppressWarnings("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.devices, container, false);

        try {
            mDevices = getBluetoothDevices();
        } catch(NotSupportedException e) {
            String message = getResources().getString(R.string.bluetooth_not_supported);

            shutdown(message);
        } catch (NotEnabledException e) {
            String message = getResources()
                    .getString(R.string.bluetooth_not_enabled);

            shutdown(message);
        }

        // Recycler view configuration.
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        RecyclerView.LayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new Adapter(mDevices, this);
        recyclerView.setAdapter(mAdapter);

        mDiscoverButton = view.findViewById(R.id.discover);
        mDiscoverButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                startDiscovery();
                return false;
            }
        });

        return view;
    }

    private List<BluetoothDevice> getBluetoothDevices() {
        mBluetoothAdapter = getBluetoothAdapter();

        BluetoothProfile.ServiceListener listener;

        listener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile,
                                           BluetoothProfile proxy) {

                if (profile == BluetoothProfile.HEADSET) {
                    mBluetoothHeadset = (BluetoothHeadset) proxy;
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.HEADSET) {
                    mBluetoothHeadset = null;
                }
            }
        };

        mBluetoothAdapter.getProfileProxy(getActivity().getApplicationContext(),
                listener, BluetoothProfile.HEADSET);

        return new ArrayList<>(mBluetoothAdapter.getBondedDevices());
    }

    private BluetoothAdapter getBluetoothAdapter() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            throw new NotSupportedException();
        }

        if (!bluetoothAdapter.isEnabled()) {
            mListener.onRequestEnableBluetooth();
        }

        return bluetoothAdapter;
    }

    /**
     * Finish the activity after a few seconds. This lets any toast message
     * show to the user or any creation during "onCreate" to finish.
     * @param message A message to be toasted to the user.
     */
    private void shutdown(String message) {
        showMessageToUser(message);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mListener.onFinish();
            }
        }, 3000);
    }

    private void showMessageToUser(String message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }

    /**
     * Initiates the discovery of bluetooth devices.
     */
    private void startDiscovery() {
        showMessageToUser(getResources().getString(R.string.discovery_started));
        mDiscoverButton.setPressed(true);
        mDiscoverButton.setEnabled(false);

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        mBluetoothAdapter.startDiscovery();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHandler.obtainMessage(Messages.END_DISCOVERY).sendToTarget();

            }
        }, 15000);
    }

    /**
     * Cancels the discovery of bluetooth devices.
     */
    private void endDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            showMessageToUser(getResources().getString(R.string.discovery_ended));
            mBluetoothAdapter.cancelDiscovery();
        }

        mDiscoverButton.setPressed(false);
        mDiscoverButton.setEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        //TODO: Cancel any runnable.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET,
                    mBluetoothHeadset);
        }

        try {
            getActivity().unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            // Ignore. Most likely receiver has not been registered.
        }
    }
}

