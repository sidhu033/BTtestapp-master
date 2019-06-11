package gamsystech.bttestapp.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.UUID;

import gamsystech.bttestapp.utils.GattAttributes;

public class RedoxerDeviceService
{
    BluetoothLeService mBluetoothLeService;
    BluetoothGattService mDeviceCommunicationService;
    BluetoothGatt  mBluetoothGatt;
    BluetoothGattCharacteristic mDeviceCommunicationCharacteristic;
    private final static String TAG = RedoxerDeviceService.class.getSimpleName();

    //constructor
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public RedoxerDeviceService(BluetoothLeService mBluetoothLeService, BluetoothGatt mBluetoothGatt)
    {
        this.mBluetoothLeService = mBluetoothLeService;
        this.mBluetoothGatt = mBluetoothGatt;


        mDeviceCommunicationService = mBluetoothGatt.getService(UUID.fromString(GattAttributes.SERVICE_GENERIC_ACCESS));
    }
    /*
    * start device if start command sent sucessfully
    *
    * */
    public boolean startDevice() throws Exception
    {
        byte[] command = new byte[]{(byte)0xBE, (byte)0xB0, 0x01, (byte)0xc0, 0x36};

       mBluetoothLeService.setCharacteristicNotification(mDeviceCommunicationCharacteristic,true);
       return  mBluetoothLeService.writeCharacteristic(mDeviceCommunicationCharacteristic,command);
    }
}
