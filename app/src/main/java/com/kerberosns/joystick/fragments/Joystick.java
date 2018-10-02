package com.kerberosns.joystick.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kerberosns.joystick.MainActivity;
import com.kerberosns.joystick.R;
import com.kerberosns.joystick.bluetooth.Reader;
import com.kerberosns.joystick.data.Mode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

// TODO: The method "finishFragment" does not toast a message right now. Better to send a message
// TODO: with the order "mActivity.onBackPressed();" a few seconds later. Otherwise it seems the
// TODO: fragment terminates and the message can not be toasted because there is not context.

// TODO: If device is not bounded, then this fragment fails during "getBluetoothSocket" because
// TODO: device is still null and trying to get the UUID generates a null pointer exception.
public class Joystick extends Fragment {
    /**
     * A static string to use as key for a parcelable.
     */
    public static final String DEVICE = "device";

    public static final String MODE = "mode";

    /**
     * The activity this fragment is attached to.
     */
    private Activity mActivity;

    /**
     * The bluetooth device the user has selected.
     */
    private BluetoothDevice mDevice;

    /**
     * The bluetooth socket that lets you communicate with the device.
     */
    private BluetoothSocket mSocket;

    /**
     * The bluetooth socket's output stream.
     */
    private OutputStream mOutputStream;

    /**
     * Text view to display the position of the X axis during a motion event.
     */
    private TextView mPosX;

    /**
     * Text view to display the position of the Y axis during a motion event.
     */
    private TextView mPosY;

    /**
     * The image view that draws the circle.
     */
    private ImageView mInnerCircle;

    /**
     * Dimensions for the inner circle: height.
     */
    private int mCircleHeight;

    /**
     * Dimensions for the inner circle: width.
     */
    private int mCircleWidth;

    /**
     * X coordinate of the inner circle's centre.
     */
    private int mCircleCentreX;

    /**
     * Y coordinate of the inner circle's centre.
     */
    private int mCircleCentreY;

    /**
     * Min/Max values for X and Y coordinates.
     */
    private int mMinX, mMaxX, mMinY, mMaxY;

    /**
     * Radius of the outer circle. This sets the boundary of the inner circle's movement.
     */
    private double mRadius;

    /**
     * Constant to signal the first quadrant.
     */
    private static final int FIRST_QUADRANT = 0;

    /**
     * Constant to signal the second quadrant.
     */
    private static final int SECOND_QUADRANT = 1;

    /**
     * Constant to signal the third quadrant.
     */
    private static final int THIRD_QUADRANT = 2;

    /**
     * Constant to signal the fourth quadrant.
     */
    private static final int FOURTH_QUADRANT = 3;

    /**
     * An integer that determines how much the power to the left and right motors needs to be
     * amended. It's ranged between 0 and 100. As an idea:
     */
    private int mBalance;

    private Mode mMode;

    /**
     * During tests, I found Android can send enough messages for the bluetooth channel to saturate.
     * This occurs because for every single pixel change, the application sends a message and if the
     * other end does not read them fast enough then, the write channel saturates and the
     * application stops responding.
     *
     * I use this attribute to only send a coordinate message when the new coordinates are
     * grater than a certain value.
     */
    private int[] lastEffectiveCoordinate = {-1, -1};

    /**
     * Factory pattern.
     *
     * @return A new instance of this class.
     */
    public static Joystick newInstance() {
        return new Joystick();
    }

    public interface Messages {
        /**
         * Signal that a message needs to be read.
         */
        int READ = 0;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            switch(message.what) {
                case Messages.READ:
                    mTextReader.setText((String) message.obj);
                    break;
            }
        }
    };

    private TextView mTextReader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();

        if (getArguments() != null) {
            mDevice = getArguments().getParcelable(DEVICE);
            mMode = Mode.valueOf(getArguments().getString(MODE));
        } else {
            finishFragment(R.string.bluetooth_not_found);
        }
    }

    private void finishFragment(int stringResource) {
        toast(stringResource);
        closeSocket();
        mActivity.onBackPressed();
    }

    private void closeSocket() {
        if (isDevelopment()) { return; }

        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            String message = getStringResource(R.string.bluetooth_close_socket);
            Log.e(MainActivity.TAG, message);
        }
    }

    private boolean isDevelopment() {
        return mMode.toString().equals(Mode.DEVELOPMENT.toString());
    }

    @SuppressWarnings("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.joystick, container, false);

        mPosX = view.findViewById(R.id.posX);
        mPosY = view.findViewById(R.id.posY);
        mInnerCircle = view.findViewById(R.id.innerCircle);
        mInnerCircle.post(new Runnable() {
            @Override
            public void run() {
                mCircleHeight = mInnerCircle.getHeight();
                mCircleWidth = mInnerCircle.getWidth();
            }
        });

        final ImageView outerCircle = view.findViewById(R.id.outerCircle);
        outerCircle.post(new Runnable() {
            @Override
            public void run() {
                mCircleCentreX = (int) outerCircle.getX() + outerCircle.getWidth() / 2;
                mCircleCentreY = (int) outerCircle.getY() + outerCircle.getHeight() / 2;
                mRadius = outerCircle.getHeight() / 2;

                mMinX = (int) (mCircleCentreX - mRadius);
                mMaxX = (int) (mCircleCentreX + mRadius);
                mMinY = (int) (mCircleCentreY - mRadius);
                mMaxY = (int) (mCircleCentreY + mRadius);
            }
        });

        final ConstraintLayout constraintLayout;
        constraintLayout = view.findViewById(R.id.constraintLayout);

        constraintLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                float x, y;
                switch(action) {
                    case MotionEvent.ACTION_DOWN:
                        x = motionEvent.getX();
                        y = motionEvent.getY();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        x = motionEvent.getX();
                        y = motionEvent.getY();

                        break;
                    default:
                        x = mCircleCentreX;
                        y = mCircleCentreY;
                }

                float[] coordinates = moveInnerCircle(x, y);
                int[] values = mapValues(coordinates[0], coordinates[1], mBalance);

                setTextViewsPositions(values[0], values[1]);

                if (newValuesDifferEnoughFromPrevious(values)) {
                    lastEffectiveCoordinate = values;
                    commandDevice(values[0], values[1]);
                }
                return true;
            }
        });

        setTextViewsPositions(512, 512);

        // Balance.
        final TextView indicator = view.findViewById(R.id.balanceIndicator);

        SeekBar balance = view.findViewById(R.id.balance);
        mBalance = balance.getProgress();

        balance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mBalance = i;
                String left = String.valueOf(i);
                String right = String.valueOf(100 -i);

                String text = formatTextBalance(left, right);
                indicator.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Nothing.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Nothing.
            }
        });

        view.findViewById(R.id.sequence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSequence();
            }
        });

        mTextReader = view.findViewById(R.id.textViewReader);

        return view;
    }

    private String formatTextBalance(String left, String right) {
        return String.format("%1$" + (4 - left.length()) +"s", "")
                + left
                + " / "
                + String.format("%1$" + (4 - right.length()) +"s", "")
                + right;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isDevelopment()) { return; }

        new ConnectToDeviceTask().execute(mDevice);
    }

    class ConnectToDeviceTask extends AsyncTask<BluetoothDevice, Void, BluetoothSocket> {
        private ProgressDialog mmDialog;

        @Override
        protected void onPreExecute() {
            mmDialog = new ProgressDialog(mActivity);
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
                    InputStream inputStream = mSocket.getInputStream();
                    new Reader('^', '$', inputStream, mHandler).start();

                } catch (IOException e) {
                    finishFragment(R.string.bluetooth_not_connected);
                }
            } else {
                finishFragment(R.string.bluetooth_not_connected);
            }
        }
    }

    /**
     * Toast a message for the user.
     * @param resource The string resource ID.
     */
    private void toast(int resource) {
        String message = mActivity.getResources().getString(resource);
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Sets as string for both the x and y text views the values of the motion event.
     * @param x The value to be showed in the text view x.
     * @param y The value to be showed in the text view y.
     */
    private void setTextViewsPositions(float x, float y) {
        String text = getStringResource(R.string.pos_x) + " " + String.valueOf((int) x);
        mPosX.setText(text);

        text = getStringResource(R.string.pos_y) + " " + String.valueOf((int) y);
        mPosY.setText(String.valueOf(text));
    }

    /**
     * Return a string identified by a resource id.
     */
    private String getStringResource(int resource) {
        return getResources().getString(resource);
    }

    /**
     * Position the circle at a given coordinates in pixels.
     * This method amends the position if the coordinates happen to be outside of the boundaries of
     * the outer circle.
     * @param x The axis value for the abscissa
     * @param y The axis value for the ordinate.
     */
    private float[] moveInnerCircle(float x, float y) {
        float a, b;
        double radius = calculateRadius(x, y);
        if (radius > mRadius) {
            float[] amendedCoordinates = amendCoordinates(x, y, radius);
            a = amendedCoordinates[0];
            b = amendedCoordinates[1];
        } else {
            a = x;
            b = y;
        }

        setCircleAtCoordinates(a, b);
        return new float[]{a, b};
    }

    /**
     * Calculate the radius of the coordinates.
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return The distance to the centre of the outer circle.
     */
    private double calculateRadius(float x, float y) {
        float a = Math.abs(x - mCircleCentreX);
        float b = Math.abs(y - mCircleCentreY);

        return Math.sqrt(a * a + b * b);
    }

    /**
     * Place the inner circle at the given coordinates.
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    private void setCircleAtCoordinates(float x, float y) {
        mInnerCircle.setX(x - mCircleWidth / 2);
        mInnerCircle.setY(y - mCircleHeight / 2);
        mInnerCircle.invalidate();
    }

    private float[] amendCoordinates(float x, float y, double radius) {
        double alpha;
        double a, b;
        switch (quadrant(x, y)) {
            case FIRST_QUADRANT:
                alpha = Math.acos((mCircleCentreX - x) / radius);
                a = mCircleCentreX - mRadius * Math.cos(alpha);
                b = mCircleCentreY - mRadius * Math.sin(alpha);
                break;
            case SECOND_QUADRANT:
                alpha = Math.acos((x - mCircleCentreX) / radius);
                a = mCircleCentreX + mRadius * Math.cos(alpha);
                b = mCircleCentreY - mRadius * Math.sin(alpha);
                break;
            case THIRD_QUADRANT:
                alpha = Math.acos((mCircleCentreX - x) / radius);
                a = mCircleCentreX - mRadius * Math.cos(alpha);
                b = mCircleCentreY + mRadius * Math.sin(alpha);
                break;
            default:
                alpha = Math.acos((x - mCircleCentreX) / radius);
                a = mCircleCentreX + mRadius * Math.cos(alpha);
                b = mCircleCentreY + mRadius * Math.sin(alpha);
                break;
        }

        return new float[]{(float) a, (float) b};
    }

    /**
     * Analyses what quadrant the inner circle is in.
     * @param x X coordinate of the event position.
     * @param y Y coordinate of the event position.
     * @return A constant that indicates the quadrant.
     */
    private int quadrant(float x, float y) {
        if (x < mCircleCentreX && y < mCircleCentreY) {
            return FIRST_QUADRANT;
        } else if (x > mCircleCentreX && y < mCircleCentreY) {
            return SECOND_QUADRANT;
        } else if (x < mCircleCentreX && y > mCircleCentreY) {
            return THIRD_QUADRANT;
        } else {
            return FOURTH_QUADRANT;
        }
    }

    /**
     * Sends a command to the bluetooth device. It also modifies the x axis, based on the balance.
     */
    private int[] mapValues(double x, double y, int balance) {
        int[] values = new int[]{
                map((int) x, mMinX, mMaxX, 0, 1024),
                map((int) y, mMinY, mMaxY, 1024, 0)};

        values[0] = values[0] - 400 + 8 * balance;

        if (values[0] < 0) {
            values[0] = 0;
        } else if (values[0] > 1023) {
            values[0] = 1023;
        }

        return values;
    }

    /**
     * Re-maps a number from one range to another. That is, a value of fromLow would get mapped to
     * toLow, a value of fromHigh to toHigh, values in-between to values in-between, etc.
     * @param value the number to map
     * @param fromLow the lower bound of the value’s current range
     * @param fromHigh the upper bound of the value’s current range
     * @param toLow the lower bound of the value’s target range
     * @param toHigh the upper bound of the value’s target range
     * @return The mapped value.
     */
    private int map(int value, int fromLow, int fromHigh, int toLow, int toHigh) {
        return (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow) + toLow;
    }

    private boolean newValuesDifferEnoughFromPrevious(int[] values) {
        return (Math.abs(values[0] - lastEffectiveCoordinate[0])
                + Math.abs(values[1] - lastEffectiveCoordinate[1])) > 50;
    }

    /**
     * Sends a command to the device.
     * @param x The coordinate X.
     * @param y The coordinate Y.
     */
    private void commandDevice(int x, int y) {
        write("^M:" + x + "," + y + "$");
    }

    private void write(String message) {
        if (isDevelopment()) { return; }

        try {
            Log.d(MainActivity.TAG, "Sent command: " + message);
            mOutputStream.write(message.getBytes());
        } catch (IOException e) {
            finishFragment(R.string.bluetooth_not_connected);
        } catch (NullPointerException e) {
            finishFragment(R.string.bluetooth_not_connected);
        }
    }

    private void changeSequence() {
        write("^CHANGE_SEQUENCE$");
    }
}