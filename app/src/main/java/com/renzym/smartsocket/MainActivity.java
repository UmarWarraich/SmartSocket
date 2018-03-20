package com.renzym.smartsocket;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity implements TaskCompleted{
    String networkSSID, networkPassword, socketSSID, socketPassword;

    EditText eip, eport;
    Button buttonClient, buttonScan;
    TextView serverStatus;

    WifiManager wifi;
    ListView lv;
    int size = 0;
    List<ScanResult> results;

    String ITEM_KEY = "key";
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    SimpleAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        socketSSID = "Socket";
        socketPassword = "11223344";
        networkSSID = "Renzym4G";
        networkPassword = "ren41610";

        //*** Finding Elements
        buttonScan = (Button) findViewById(R.id.buttonScan);
        buttonClient = (Button) findViewById(R.id.client);
        serverStatus = (TextView)findViewById(R.id.server_status);
        eip = (EditText)findViewById(R.id.ip);
        eport = (EditText)findViewById(R.id.port);
        lv = (ListView)findViewById(R.id.list);

        this.adapter = new SimpleAdapter(MainActivity.this, arraylist, R.layout.row, new String[] { ITEM_KEY }, new int[] { R.id.list_value });
        lv.setAdapter(this.adapter);

        //***Checking if wifi available and if not then connect it
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false) {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        //*** Registering Broadcast Receiver
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent)
            {
                results = wifi.getScanResults();
                size = results.size();
                Log.d("size0:", String.valueOf(size));
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        //**** ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                connectToWifi(socketSSID, socketPassword);
            }
        });

        //***Client Button listener
        buttonClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new Client(MainActivity.this, eip.getText().toString(), Integer.parseInt(eport.getText().toString()),networkSSID, networkPassword).execute();
            }
        });

        //***Scan Wifi Button Listener
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String netAddress = null;
                try
                {
                    netAddress = new NetTask().execute("192.168.8.1").get();
                    //System.out.println("netAddress: " + netAddress);
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }



                arraylist.clear();
                wifi.startScan();
                try {
                    results = wifi.getScanResults();

                    size = size - 1;
                    while (size >= 0) {
                        HashMap<String, String> item = new HashMap<String, String>();
                        item.put(ITEM_KEY, results.get(size).SSID + "  " + results.get(size).capabilities);
                        System.out.println(item);
                        arraylist.add(item);
                        size--;
                        adapter.notifyDataSetChanged();
                    }
                }
                catch (Exception e) { }
            }
        });

    }

    @Override
    public void onTaskComplete(String result) {
        System.out.println(result);
        Toast.makeText(this,"The result is " + result,Toast.LENGTH_LONG).show();
        if(result.equalsIgnoreCase("connectionDone")) {
            connectToWifi(networkSSID, networkPassword);
           new CountDownTimer(30000, 1000) {

               public void onTick(long millisUntilFinished) {
                   serverStatus.setText("seconds remaining: " + millisUntilFinished / 1000);
               }

               public void onFinish() {
                   serverStatus.setText("done!");
                   new DoPing(MainActivity.this, getLocalIpAddress(), Integer.parseInt(eport.getText().toString())).execute();
               }
           }.start();

//                wait(10000);


        }
    }
    public class NetTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                for(int i = 109; i < 255; i++) {
                    String lastThree = params[0].substring(params[0].length() - 3, params[0].length());
                    System.out.println(lastThree);
                    String clientIP = params[0].substring(0, (params[0].length() - 3)) + i;
                    InetAddress addr = InetAddress.getByName(clientIP);

                    boolean status = addr.isReachable(5000); //Timeout = 5000 milli seconds

                    if (status)
                    {
                        System.out.println("Status : Host is reachable");
                    }
                    else
                    {
                        System.out.println("Status : Host is not reachable");
                    }
                }
            }

            catch (UnknownHostException e)
            {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "a";
        }
    }
    // GETS THE IP ADDRESS OF YOUR PHONE'S NETWORK
    private String getLocalIpAddress() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        return ip;
    }
    private void connectToWifi(String SSId, String password) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + SSId + "\"";
        conf.preSharedKey = "\""+ password +"\"";

        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + SSId + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

               Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }
}