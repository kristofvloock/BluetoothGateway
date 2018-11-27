package com.cw2b2.bluetoothgateway;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class Main2Activity extends AppCompatActivity {
    TextView textview;
    // Button btnOn, btnOff, btnDis;
    Button On, Off, Discnt;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    byte[] buffer;
    int bytes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra("EXTRA_ADDRESS"); //receive the address of the bluetooth device

        //view of the ledControl
        setContentView(R.layout.activity_main2);

        //call the widgets
        On = (Button)findViewById(R.id.button5);
        Off = (Button)findViewById(R.id.button6);
        Discnt = (Button)findViewById(R.id.button7);
        textview = (TextView)findViewById(R.id.textView2);

        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        On.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getGPS();      //method to turn on
            }
        });

        Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getSecret();   //method to turn off
            }
        });

        Discnt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        new Thread( new Runnable(){
        @Override
        public void run(){
            Looper.prepare();
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String gps = getGPS();
                String secret = getSecret();

                requestContract(secret, gps);
                Log.d("...---...", "GPS: " + gps + " Secret: " + secret);
            }
            }
        }).start();

    }


    private void requestContract(final String secret, final String gps) {

        String url = "https://andreasp.ulyssis.be/auth/bikeMessage/2/";
        RequestQueue queue = Volley.newRequestQueue(this);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("CW2B2", error.toString());
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String headerString = "Token 16f397bb14e5ef2f5c3073cf385c36c7774fff32";
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("Authorization", headerString);
                return headers;
            }
            @Override
            protected Map<String, String > getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("secret", secret);
                params.put("gpgga", gps);
                return params;
            }

        };
        queue.add(stringRequest);
    }


    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }

    private String getSecret()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("1".getBytes());



                InputStream input = btSocket.getInputStream();
                DataInputStream dinput = new DataInputStream(input);
                while (dinput.available() == 0) {
                    TimeUnit.SECONDS.sleep(1);
                }
                TimeUnit.SECONDS.sleep(1);
                buffer = new byte[dinput.available()];
                dinput.read(buffer);
            }
            catch (IOException e)
            {
                msg("Error");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new String(buffer);
    }


    private String getGPS()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("0".getBytes());



                InputStream input = btSocket.getInputStream();
                DataInputStream dinput = new DataInputStream(input);
                while (dinput.available() == 0) {
                    TimeUnit.SECONDS.sleep(1);
                }
                TimeUnit.SECONDS.sleep(1);
                buffer = new byte[dinput.available()];
                dinput.read(buffer);
            }
            catch (IOException e)
            {
                msg("Error");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new String(buffer);
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(Main2Activity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}


