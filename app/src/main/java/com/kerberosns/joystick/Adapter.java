package com.kerberosns.joystick;


import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    /**
     * List of devices.
     */
    private final List<BluetoothDevice> mDevices;

    /**
     * The listener that triggers what to do what the user clicks on an
     * adapter's view that contains a bluetooth device.
     */
    public interface OnDeviceClickListener {
        void onClick(BluetoothDevice device);
    }
    /**
     * The listener to trigger when the user presses on a list item.
     */
    private final OnDeviceClickListener mListener;

    /**
     * The constructor.
     * @param devices The list of bluetooth devices.
     * @param listener A listener to trigger when the user presses on a list
     *                 item.
     */
    public Adapter(List<BluetoothDevice> devices, OnDeviceClickListener listener) {
        mDevices = devices;
        mListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mPrimary;
        final TextView mSecondary;

        ViewHolder(View view) {
            super(view);
            mPrimary = view.findViewById(R.id.primary);
            mSecondary = view.findViewById(R.id.secondary);
        }
    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                 final int viewType) {

        LinearLayout linearLayout = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.adapter_item, parent, false);

        return new ViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        String text;

        final BluetoothDevice device = mDevices.get(position);

        text = device.getName();
        viewHolder.mPrimary.setText(text);

        text = device.getAddress();
        viewHolder.mSecondary.setText(text);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onClick(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }
}
