package com.kerberosns.joystick.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.kerberosns.joystick.MainActivity;
import com.kerberosns.joystick.R;
import com.kerberosns.joystick.data.Mode;

public class Settings extends Fragment {
    /**
     * All configurable actions that the calling activity needs to implement.
     */
    public interface Configurable {
        /**
         * When the user selects a specific mode.
         */
        void onModeSelected(Mode mode);

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

    /** Activity's context. */
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Configurable) {
            mListener = (Configurable) context;
        } else {
            String message = getResources().getString(R.string.implementation_exception);
            throw new ClassCastException(message);
        }

        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.settings, container, false);

        Spinner spinner = view.findViewById(R.id.modes);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(
                        mContext,
                        R.array.modes,
                        R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String name = (String) adapterView.getItemAtPosition(i);
                mListener.onModeSelected(Mode.valueOf(name));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            String mode = bundle.getString(MainActivity.MODE);
            if (mode != null) {
                if (mode.equals(Mode.REAL.toString())) {
                    spinner.setSelection(0);
                } else {
                    spinner.setSelection(1);
                }
            }
        }

        final TextView distribution = view.findViewById(R.id.distributionTextView);

        SeekBar seekBar = view.findViewById(R.id.distributionSeekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mListener.onDistributionChanged(progress);
                updateDistributionTextView(distribution, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mListener.onDistributionChanged(seekBar.getProgress());
                updateDistributionTextView(distribution, seekBar.getProgress());
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

        if (length == 1) {
            text = "  " + left;
        } else if (length == 2) {
            text = " " + left;
        } else {
            text = left;
        }

        text += " / ";
        length = right.length();

        if (length == 1) {
            text += "   " + right;
        } else if (length == 2) {
            text += "  " + right;
        } else {
            text += right;
        }

        distribution.setText(text);
    }
}
