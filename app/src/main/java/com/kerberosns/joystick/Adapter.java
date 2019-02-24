package com.kerberosns.joystick;


import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
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
     * The application resources.
     */
    private final Resources mResources;

    /**
     * The constructor.
     * @param devices The list of bluetooth devices.
     * @param resources The application resources.
     * @param listener A listener to trigger when the user presses on a list
     *                 item.
     */
    public Adapter(List<BluetoothDevice> devices, Resources resources,
                   OnDeviceClickListener listener) {

        mDevices = devices;
        mResources = resources;
        mListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mPrimary;
        final TextView mSecondary;
        final TextView mTertiary;

        ViewHolder(View view) {
            super(view);
            mPrimary = view.findViewById(R.id.primary);
            mSecondary = view.findViewById(R.id.secondary);
            mTertiary = view.findViewById(R.id.tertiary);
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

        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            text = mResources.getString(R.string.state_not_bounded);
        } else {
            text = mResources.getString(R.string.state_bounded);
        }
        viewHolder.mTertiary.setText(text);

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
