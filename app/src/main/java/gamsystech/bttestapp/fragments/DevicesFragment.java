package gamsystech.bttestapp.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import gamsystech.bttestapp.R;

public class DevicesFragment extends Fragment
{
    private final BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver bleDiscoveryBroadcastReceiver;
    private IntentFilter bleDiscoveryIntentFilter;

    private ArrayList<BluetoothDevice> listitems = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> listadapter;

    public DevicesFragment()
    {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
       View rootview = inflater.inflate(R.layout.configdevice,container,false);
       return rootview;

    }

    @Override
    public void onResume()
    {
        super.onResume();
    }
}
