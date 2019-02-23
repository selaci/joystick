package com.kerberosns.joystick;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.kerberosns.joystick.data.Allocator;
import com.kerberosns.joystick.data.Encoder;

public abstract class Joystick extends AppCompatActivity {
    /*
     * Things that I need for the vertical axis.
     *
     * The top Y pixel.
     * The bottom Y pixel.
     * The centre Y of the axis.
     * The height Y of the joystick.
     */
    private int mTopY;
    private int mBottomY;
    private int mCentreY;
    private int mHeightY;

    private ImageView mVerticalAxis;
    private ImageView mVerticalJoystick;

    /*
     * This that I need for the horizontal axis.
     *
     * The left X pixel.
     * The right X pixel.
     * The centre X of the axis.
     * The width X of the joystick.
     */
    private int mLeftX;
    private int mRightX;
    private int mCentreX;
    private int mWidthX;

    private ImageView mHorizontalAxis;
    private ImageView mHorizontalJoystick;

    /** The number of blocks each axis is divided by. */
    private int mNumOfBlocks = 6;

    /**
     * The block number that is in the middle of the axis. This is equivalent to not power.
     * It can be calculated programmatically, but I believe this is more explicit.
     */
    private byte mNeutral = 3;

    /** It maps positions in the vertical axis to block numbers. */
    private Allocator mVerticalAllocator;

    /** It maps positions in the horizontal axis to block numbers. */
    private Allocator mHorizontalAllocator;

    /**
     * I use this variable to see if the new moment command is the same one that has been sent
     * in the past, if so then, I don't send the command.
     *
     * The main reason behind this is to avoid buffer saturation. */
    private byte mSavedHorizontalBlockNumber = 3;
    private byte mSavedVerticalBlockNumber = 3;

    /** Save the state of the horizontal joystick. */
    private boolean mHorizontalJoystickPressed = false;

    /** The current motor distribution. */
    private int mDistribution;

    /** It encodes commands into bytes that can be transmitted over the serial channel. */
    private Encoder mEncoder = new Encoder();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick);

        mVerticalAxis = findViewById(R.id.verticalAxisView);
        mVerticalJoystick = findViewById(R.id.verticalJoystick);

        mVerticalAxis.post(new Runnable() {
            @Override
            public void run() {
                mTopY = (int) mVerticalAxis.getY();
                mBottomY = mTopY + mVerticalAxis.getHeight();
                mCentreY = (mTopY + mBottomY) / 2;
            }
        });

        mVerticalJoystick.post(new Runnable() {
            @Override
            public void run() {
                mHeightY = mVerticalJoystick.getHeight();
                mVerticalAllocator = new Allocator(mVerticalAxis.getHeight(), mNumOfBlocks, mTopY);
            }
        });

        ConstraintLayout layout = findViewById(R.id.verticalJoystickConstraintLayout);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                double y;
                switch(action) {
                    case MotionEvent.ACTION_DOWN:
                        y = event.getY() - (mHeightY / 2.0);

                        break;
                    case MotionEvent.ACTION_MOVE:
                        y = event.getY() -  (mHeightY / 2.0);

                        break;
                    default:
                        y = mCentreY - (mHeightY / 2.0);
                }

                if (y < mTopY - mHeightY/2.0) {
                    y = (mTopY - mHeightY/2.0);
                } else if (y > (mBottomY - mHeightY/2.0)) {
                    y = (mBottomY - mHeightY/2.0);
                }

                mVerticalJoystick.setY((float) y);
                mVerticalJoystick.invalidate();

                // Calculate the centre of the joystick.
                y += mHeightY / 2.0;

                sendVerticalMoveCommand((int) y);

                return true;
            }
        });



        // Horizontal axis.
        mHorizontalAxis = findViewById(R.id.horizontalAxisView);
        mHorizontalJoystick = findViewById(R.id.horizontalJoystick);

        mHorizontalAxis.post(new Runnable() {
            @Override
            public void run() {
                mLeftX = (int) mHorizontalAxis.getX();
                mRightX = mLeftX + mHorizontalAxis.getWidth();
                mCentreX = (mLeftX + mRightX) / 2;
            }
        });

        mHorizontalJoystick.post(new Runnable() {
            @Override
            public void run() {
                mWidthX = mHorizontalJoystick.getWidth();
                mHorizontalAllocator = new Allocator(mHorizontalAxis.getWidth(), mNumOfBlocks, mLeftX);
            }
        });


        layout = findViewById(R.id.horizontalJoystickConstraintLayout);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                double x;
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mHorizontalJoystickPressed = true;
                        // There is no break, because it's actually the same logic.
                    case MotionEvent.ACTION_MOVE:
                        x = (event.getX() - mWidthX/2.0);

                        if (x < (mLeftX - mWidthX/2.0)) {
                            x = (mLeftX - mWidthX/2.0);
                        } else if (x > mRightX - mWidthX/2.0) {
                            x = (mRightX - mWidthX/2.0);
                        }

                        mHorizontalJoystick.setX((float) x);
                        mHorizontalJoystick.invalidate();

                        // Calculate the centre of the joystick.
                        x += mWidthX / 2.0;

                        sendHorizontalMoveCommand((int) x);

                        break;
                    default:
                        x = (mCentreX - mWidthX/2.0);
                        mHorizontalJoystick.setX((float) x);
                        mHorizontalJoystick.invalidate();

                        restoreVerticalMovement();

                        mHorizontalJoystickPressed = false;
                        break;
                }

                return true;
            }
        });

        Button button = findViewById(R.id.changeSequence);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSequence();
            }
        });

        if (savedInstanceState != null) {
            mDistribution = savedInstanceState.getInt(MainActivity.DISTRIBUTION);
        } else {
            Intent intent = getIntent();
            mDistribution = intent.getIntExtra(MainActivity.DISTRIBUTION, 50);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(MainActivity.DISTRIBUTION, mDistribution);
    }


    /**
     * Sends a command to change the LED sequence.
     */
    protected void changeSequence() {
        byte encoded = mEncoder.changeLedSequence();
        write(encoded);
    }

    private void sendVerticalMoveCommand(int pos) {
        byte blockNumber = mVerticalAllocator.allocate(pos);

        if (mSavedVerticalBlockNumber != blockNumber) {
            mSavedVerticalBlockNumber = blockNumber;

            if (!mHorizontalJoystickPressed) {
                sendMoveCommand(blockNumber, mNeutral);
            }
        }
    }


    private void sendHorizontalMoveCommand(int pos) {
        byte blockNumber = mHorizontalAllocator.allocate(pos);
        if (mSavedHorizontalBlockNumber != blockNumber) {
            mSavedHorizontalBlockNumber = blockNumber;
            sendMoveCommand(mNeutral, blockNumber);
        }
    }

    /**
     * Sends a command to the device.
     * @param x The coordinate X.
     * @param y The coordinate Y.
     */
    private void sendMoveCommand(byte x, byte y) {
        byte encoded = mEncoder.move(x, y);
        write(encoded);
    }

    /**
     * Sends the message trough the channel.
     * @param encoded The byte that contains the message encoded.
     */
    protected abstract void write(byte encoded);


    /**
     * Send the previous vertical movement command. This is used when the horizontal joystick no
     * longer applies, for example if the user has raised up the finger.
     */
    private void restoreVerticalMovement() {
        sendMoveCommand(mNeutral, mSavedVerticalBlockNumber);
    }

    protected void setMotorDistribution() {
        byte encoded = mEncoder.setMotorDistribution(mDistribution);
        write(encoded);
    }
}
