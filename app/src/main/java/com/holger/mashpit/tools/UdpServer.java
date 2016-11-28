package com.holger.mashpit.tools;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.os.StrictMode;
import android.util.Log;

public class UdpServer {

    private static final String DEBUG_TAG = "UdpServer";
    private static final int UDP_SERVER_PORT = 11111;
    private static final int MAX_UDP_DATAGRAM_LEN = 1500;

    private String senderIP="";
    private String message="";

    private DatagramSocket socket=null;

    public boolean runUdpServer() {
        Log.i(DEBUG_TAG,"UDP Server started");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            InetAddress broadcastIP = InetAddress.getByName("255.255.255.255");

            byte[] recvBuf = new byte[MAX_UDP_DATAGRAM_LEN];

            socket = new DatagramSocket(UDP_SERVER_PORT, broadcastIP);
            socket.setBroadcast(true);

            socket.setSoTimeout(20000);
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
            Log.e(DEBUG_TAG, "Waiting for UDP broadcast");
            socket.receive(packet);

            senderIP = packet.getAddress().getHostAddress();
            message = new String(packet.getData()).trim();

            Log.e(DEBUG_TAG, "Got UDB broadcast from " + senderIP + ", message: " + message);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

    }

    public String getSenderIP()
    {
        return senderIP;
    }
    public String getMessage()
    {
        return message;
    }

    public void closeSocket() {
        if (socket != null) {
            socket.close();
        }
    }
}
