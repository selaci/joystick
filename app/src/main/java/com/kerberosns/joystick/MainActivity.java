package com.kerberosns.joystick;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.kerberosns.joystick.data.Mode;
import com.kerberosns.joystick.fragments.Devices;
import com.kerberosns.joystick.fragments.Settings;

public class MainActivity extends AppCompatActivity implements
        Devices.Listener, Settings.Configurable {

    /**
     * A constant for debugging.
     */
    public static final String TAG = "JOYSTICK";

    public static final String MODE = "mode";

    /**
     * The drawer layout.
     */
    private DrawerLayout mDrawerLayout;

    /**
     * Activity for result.
     */
    private final static int REQUEST_ENABLE_BLUETOOTH = 0;

    /**
     * Default mode value.
     */
    private Mode mMode = Mode.REAL;

    /** The static key for passing a bluetooth device between activities. */
    public static final String BLUETOOTH = "bluetooth";

    @Override
    public void onRequestEnableBluetooth() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode != RESULT_OK) {
                showMessageToUser(R.string.bluetooth_not_enabled);
                finish();
            } else {
                Fragment fragment = Devices.newInstance();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            }
        }
    }

    private void showMessageToUser(int resource) {
        String message = getResources().getString(resource);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFinish() {
        finish();
    }

    @Override
    public void onBluetoothDeviceSelected(BluetoothDevice device) {
        Intent intent;
        if (mMode.equals(Mode.TEST)) {
            intent = new Intent(getBaseContext(), TestJoystick.class);
        } else {
            intent = new Intent(getBaseContext(), RealJoystick.class);
            intent.putExtra(MainActivity.BLUETOOTH, device);
        }

        startActivity(intent);
    }


    @Override
    public void onModeSelected(Mode mode) {
        mMode = mode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolBar();

        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        select(menuItem);
                        return false;
                    }
                });

        // Add the devices fragment as a default one.
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content_frame, Devices.newInstance()).commit();
    }

    /**
     * Set the tool bar as a support action bar for the main activity.
     */
    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    /**
     * Set a frame on the activity based on the item the user has selected.
     * @param menuItem The selected item.
     */
    private void select(MenuItem menuItem) {
        Fragment fragment;

        switch(menuItem.getItemId()) {
            case R.id.nav_devices:
                fragment = Devices.newInstance();
                break;
            case R.id.nav_setup:
                fragment = Settings.newInstance();
                Bundle bundle = new Bundle();
                bundle.putString(MODE, mMode.toString());
                fragment.setArguments(bundle);
                break;
            default:
                fragment = Devices.newInstance();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        menuItem.setChecked(true);

        setTitle(menuItem.getTitle());

        mDrawerLayout.closeDrawers();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}