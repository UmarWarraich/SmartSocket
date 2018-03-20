package com.renzym.smartsocket;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Hammad on 1/27/2018.
 */

public class DoPing extends AsyncTask<Void, Void, String> {
    private TaskCompleted mCallback;

    private Context mContext;
    private Socket clientSocket;
    String clientIP, dataReceived, result, originalIP;
    int cientPort;

    DataOutputStream outToServer;
    BufferedReader inFromServer;

    JSONObject pingData;
    public DoPing(Context context, String IP, int port){
        this.mContext = context;
        this.mCallback = (TaskCompleted) context;

        result = null;
        pingData = new JSONObject();

        dataReceived = "";

        this.cientPort = port;
        this.clientIP = IP;
        this.originalIP = IP;
        //***Making JSON Data for sending on server
        try {
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
    private boolean executeCommand(String iptoCheck){
        System.out.println("executeCommand");
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 " + iptoCheck);
            int mExitValue = mIpAddrProcess.waitFor();
            System.out.println(" mExitValue "+mExitValue);
            if(mExitValue==0){
                return true;
            }else{
                return false;
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            System.out.println(" Exception:"+ignore);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println(" Exception:"+e);
        }
        return false;
    }
    public static String runSystemCommand(String command) {
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader inputStream = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

            String s = inputStream.readLine();
            System.out.println("pingOut: "+ s);
//             reading output stream of the command
//            while ((s = inputStream.readLine()) != null) {
//                System.out.println(s);
//            }
            return s ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected String doInBackground(Void... voids) {
        for (int i = 2; i < 254; i++) {
            clientIP = clientIP.substring(0, (clientIP.length() - 3)) + i;
            //dataReceived = runSystemCommand("ping " + clientIP);
            boolean dR = executeCommand(clientIP);

            System.out.println("dataReceived: "+ dR);

            if (dataReceived.contains("msg")) {
                System.out.println(dataReceived);
                return "pingDone";
            }
            clientIP = originalIP;
            System.out.println("dataReceived1: "+ dataReceived);
        }
        return null;
    }
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        System.out.println("pingDone: "+ s);
        result = s;
        mCallback.onTaskComplete(result);
    }
}