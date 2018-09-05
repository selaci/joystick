package com.kerberosns.joystick.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.kerberosns.joystick.R;

public class Settings extends Fragment {
    /**
     * All configurable actions that the calling activity needs to implement.
     */
    public interface Configurable {
        /**
         * When the user selects a specific mode.
         */
        void onModeSelected(String mode);
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

        Spinner spinner = view.findViewById(R.id.modes);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(
                        getActivity().getApplicationContext(),
                        R.array.modes,
                        android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String mode = (String) adapterView.getItemAtPosition(i);
                mListener.onModeSelected(mode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return view;
    }
}
