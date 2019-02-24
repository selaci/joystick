package com.kerberosns.joystick.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.kerberosns.joystick.MainActivity;
import com.kerberosns.joystick.R;

public class Settings extends Fragment {
    /**
     * All configurable actions that the calling activity needs to implement.
     */
    public interface Configurable {
        /**
         * When the user switches on or off the development mode.
         */
        void onModeSelected(boolean developmentMode);

        /**
         * When the user drags the seek bar.
         * @param progress The current progress selection.
         */
        void onDistributionChanged(int progress);
    }

    /**
     * Factory method constructor.
     * @return An instance of this fragment.
     */
    public static Settings newInstance() {
        return new Settings();
    }

    private Configurable mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Configurable) {
            mListener = (Configurable) context;
        } else {
            String message = getResources().getString(R.string.implementation_exception);
            throw new ClassCastException(message);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.settings, container, false);

        Switch devModeSwitch = view.findViewById(R.id.developmentModeSwitch);

        int distribution = 50;

        Bundle bundle = getArguments();
        if (bundle != null) {
            boolean developmentMode = bundle.getBoolean(MainActivity.MODE);
            if (developmentMode) {
                devModeSwitch.setChecked(true);
            } else {
                devModeSwitch.setChecked(false);
            }

            distribution = bundle.getInt(MainActivity.DISTRIBUTION);
        }

        devModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mListener.onModeSelected(b);
            }
        });

        final TextView balance = view.findViewById(R.id.balance);

        SeekBar seekBar = view.findViewById(R.id.powerDistributionSeekBar);
        seekBar.setProgress(distribution);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mListener.onDistributionChanged(progress);
                updateDistributionTextView(balance, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mListener.onDistributionChanged(seekBar.getProgress());
                updateDistributionTextView(balance, seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return view;
    }

    private void updateDistributionTextView(TextView distribution, int progress) {
        String left = Integer.toString(150 - progress);
        String right = Integer.toString(50 + progress);

        String text;

        int length = left.length();

        switch (length) {
            case 1:
                text = "  " + left;
                break;
            case 2:
                text = " " + left;
                break;
            default:
                text = left;
                break;
        }

        text += " / ";
        length = right.length();

        switch (length) {
            case 1:
                text += "  " + right;
                break;
            case 2:
                text += " " + right;
                break;
            default:
                text += right;
                break;
        }

        distribution.setText(text);
    }
}
