package com.renzym.smartsocket;

import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.net.*;

/**
 * Created by Hammad on 11/6/2017.
 */
public class Client extends AsyncTask<Void, Void, String> {
    private TaskCompleted mCallback;

    private Context mContext;
    private  Socket clientSocket;
    String clientIP, SSID, password, dataReceived, result;
    int cientPort;

    DataOutputStream outToServer;
    BufferedReader inFromServer;

    JSONObject postData, handShakeData, pingData;
    public Client(Context context, String IP, int port, String SSID, String password){
        this.mContext = context;
        this.mCallback = (TaskCompleted) context;

        result = null;
        postData = new JSONObject();
        handShakeData = new JSONObject();
        pingData = new JSONObject();

        dataReceived = "";

        this.cientPort = port;
        this.clientIP = IP;
        this.SSID = SSID;
        this.password = password;

        //***Making JSON Data for sending on server
        try {
            handShakeData.put("msg", 0);

            postData.put("msg", 1);
            postData.put("SSID", SSID);
            postData.put("PASSWORD", password);

            pingData.put("msg", 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void closeAllData() throws IOException {
        outToServer.flush();
        outToServer.close();
        inFromServer.close();
        clientSocket.close();
    }
    private void openAllData() throws IOException {
        clientSocket = new Socket(clientIP, cientPort); //*** Open Socket in client mode
        outToServer = new DataOutputStream(clientSocket.getOutputStream()); //***Stream for sending data
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // /****Stream for receiving data from server
    }
    @Override
    protected String doInBackground(Void... voids) {
        try {
            openAllData();

            outToServer.writeBytes(handShakeData.toString());
            dataReceived = inFromServer.readLine(); //**** Data reveived from Server

            if (dataReceived.equalsIgnoreCase("80")) {
                closeAllData();
                openAllData();

                outToServer.writeBytes(postData.toString());
                dataReceived = "";
                dataReceived = inFromServer.readLine(); //**** Data reveived from Server
                if(dataReceived.equalsIgnoreCase("81")) {
                    System.out.println("PD Possible: "+ dataReceived);
                    return "connectionDone";
                } else {
                    System.out.println("PD Not Possible: " + dataReceived);
                }
            } else {
                System.out.println("Not Possible: " + dataReceived);
            }

            closeAllData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        System.out.println("connected on Home network: "+ s);
        result = s;
        mCallback.onTaskComplete(result);
    }
}