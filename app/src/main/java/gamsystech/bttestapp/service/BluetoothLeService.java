package gamsystech.bttestapp.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import gamsystech.bttestapp.utils.GattAttributes;

public class BluetoothLeService extends Service
{
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    public BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_CONNECTED;

    private final IBinder mBinder = new LocalBinder();              //instance of IBinder
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT);

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =new BluetoothGattCallback()
    {
        @SuppressLint("NewApi")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {

            Log.i(TAG, "onConnectionStateChange: status, newState: "+status+", "+newState);
            String intentAction;

            if(newState ==BluetoothProfile.STATE_CONNECTED)
            {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);                          //send broadcast update to intent
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
            }
            else if(newState==BluetoothProfile.STATE_DISCONNECTED)
            {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);                              //send broadcast update to intent
            }

        }


        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if(status== BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
            else
            {
                Log.w(TAG,"on service Discovery Recived"+status);
            }

        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            if(status== BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);

            }
            Log.d(TAG, "onCharacteristicRead: characteristic="+characteristic.getUuid()+"| value="+characteristic.getValue().toString());

        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {

            broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);
        }
    };

    //local Binder Bluetoooth class
    public class LocalBinder extends Binder
    {
        public BluetoothLeService getService()
        {
            return BluetoothLeService.this;
        }
    }

    //broadcast update method for specific bind
    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    //this is specific handling for the heart rate meaurament profile.
    //data passing activity is carried out as per profile specification.
    // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void broadcastUpdate(final String action , final BluetoothGattCharacteristic characteristic)
    {
        final Intent intent = new Intent(action);
        final  byte[] data = characteristic.getValue();
        intent.putExtra(EXTRA_DATA, data);
        sendBroadcast(intent);                      //charcterstic data to intent in byte[]
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;                     //instance of IBinder
    }

    // After using a given device, you should make sure that BluetoothGatt.close() is called
    // such that resources are cleaned up properly.  In this particular example,
    // close() is invoked when the UI is disconnected from the Service.

    @Override
    public boolean onUnbind(Intent intent)
    {
        //  close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean initialize()
    {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.

        if(mBluetoothManager == null)
        {
            getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager ==null)
            {
                Log.e(TAG,"Unable to Initlize Bluetooth Manager");

                return false;
            }

        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address)
    {
        if (mBluetoothAdapter == null || address == null)
        {
            Log.w(TAG, "BLuetoothAdapter not initilized or unspecified address");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null)
        {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            {
                if(mBluetoothGatt.connect())
                {
                    mConnectionState = STATE_CONNECTING;
                    return true;
                }
                else
                {
                    return false;
                }
            }

        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if(device == null)
        {
            Log.w(TAG,"DEvice not found. unable to connect");
            return  false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            mBluetoothGatt = device.connectGatt(this,false,mGattCallback);
            mBluetoothDeviceAddress = address;
            mConnectionState = STATE_CONNECTING;
            Log.d(TAG, "Trying to create a new connection.");
        }
        return true;

    }
    //connect method closed

    /**
     * Check if already connected to device
     *
     * @param address   Address of device
     * @return
     */
    public boolean isConnected(final String address)
    {
        if (mBluetoothAdapter == null || address == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null)
        {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            {
                if (mBluetoothGatt.connect())
                {
                    mConnectionState = STATE_CONNECTING;
                    return true;
                } else
                {
                    return false;
                }
            }
        }
        else
        {
            return false;
        }

    return  true;
    }
    //isconnected closed

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */

    public void disconnect()
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothGatt.disconnect();
        }
    }
    //disconnected existing connection closed

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */

    public void close()
    {
        if (mBluetoothGatt == null)
        {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothGatt.close();
        }
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */

    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,boolean enabled)
    {
        if(mBluetoothAdapter == null  ||mBluetoothGatt ==null)
        {
            Log.w(TAG,"Bluetooth not initilized");
           return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothGatt.setCharacteristicNotification(characteristic,enabled);
        }

        //this is specific to heart rate measurements

        if(UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid()))
        {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }
    }
    //set charterstics method closed

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public List<BluetoothGattService> getSupportedGattServices()
    {
        if(mBluetoothGatt == null)
            return null;

            return mBluetoothGatt.getServices();
    }
    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}. The write result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to write on.
     */
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic,byte[] value)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        Log.w(TAG, "Writing value: "+(value) + " to "+ characteristic.getUuid());
        characteristic.setValue(value);
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }
    /*Utility function for printing byte array to hex string*/
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}

