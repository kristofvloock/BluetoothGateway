package com.cw2b2.bluetoothgateway;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    Button b1, b2, b3, b4;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button)findViewById(R.id.button);
        b2 = (Button)findViewById(R.id.button2);
        b3 = (Button)findViewById(R.id.button3);
        b4 = (Button)findViewById(R.id.button4);

        BA = BluetoothAdapter.getDefaultAdapter();
        listview = (ListView)findViewById(R.id.listview);
    }

    public void on(View view) {
        if(!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(turnOn);
            Toast.makeText(this, "Turned On", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Already On", Toast.LENGTH_LONG).show();
        }
    }

    public void off(View view) {
        BA.disable();
        Toast.makeText(this, "Turned Off", Toast.LENGTH_LONG).show();
    }

    public void visible(View view) {
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivity(getVisible);
    }

    public void list(View view) {
        pairedDevices = BA.getBondedDevices();

        ArrayList list = new ArrayList();

        for (BluetoothDevice BD : pairedDevices) {
            list.add(BD.getName() + "\n" + BD.getAddress());
        }

        Toast.makeText(this, "Show Paired Devices", Toast.LENGTH_LONG).show();

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);

        listview.setAdapter(adapter);
        listview.setOnItemClickListener(myListClickListner);
    }

    public AdapterView.OnItemClickListener myListClickListner = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            Log.d("Blah", address);

            Intent i = new Intent(MainActivity.this, Main2Activity.class);
            i.putExtra("EXTRA_ADDRESS", address);
            startActivity(i);
        }
    };



}
