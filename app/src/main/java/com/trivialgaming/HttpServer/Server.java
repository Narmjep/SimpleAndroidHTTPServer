package com.trivialgaming.HttpServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.lang.Thread;
import java.util.ArrayList;

import android.content.res.AssetManager;
import android.os.health.SystemHealthManager;

import androidx.versionedparcelable.ParcelField;


class Server {
    public String IP;
    private int port = 8080;
    final static int n_threads = 0;
    final static int max_threads = 2;
    private boolean running;
    private ArrayList<ServerListener> threads = new ArrayList<ServerListener>();
    //Objects
    private ServerSocket serverSocket;
    private AssetManager assetManager;
    //Threads
    InitSocket_t initSocket_thd;
    GetIP_t getIP_thd;
    public StartServer_t startServer_thd;

    //Locks
    private final Object lock = new Object();
    private final Object Lock_serverSocket = new Object();
    private final Object Lock_Assets = new Object();
    private final Object Lock_running = new Object();

    // ---------------------- METHODS ---------------------- //
    Server(AssetManager am){
        assetManager = am;
    }

    public ServerSocket getServerSocket(){
        return serverSocket;
    }

    public void Init() throws IOException{
        getIP_thd = new GetIP_t();
        getIP_thd.start();
        //Wait for IP
        try {
            getIP_thd.join();
        } catch (InterruptedException e) {
            System.err.println("Failed to join threads");
            e.printStackTrace();
        }
    }

    public void run(){
        startServer_thd = new StartServer_t(this);
        startServer_thd.start();
    }

    public class StartServer_t extends Thread{
        private Server server;
        StartServer_t(Server server){
            this.server = server;
        }
        @Override
        public void run()
        {
            running = true;
            initSocket_thd = new InitSocket_t();
            initSocket_thd.start();
            System.out.println("Waiting for socket...");
            try {
                initSocket_thd.join();
            } catch (InterruptedException e) {
                System.err.println("Failed to join threads");
                e.printStackTrace();
            }
            //Socket initialized
            System.out.println("Socket has been initialized");

            while (running) {
                //Socket socket = serverSocket.accept();
                if (threads.size() < max_threads) {
                    System.out.println("Starting new listening thread");
                    threads.add(new ServerListener(server, serverSocket)); //change server to this
                    threads.get(threads.size() - 1).start();
                }
            }
            System.out.println("Shutting down server...");
        }
    }

    private class InitSocket_t extends Thread {
        @Override
        public void run() {
            try {
                System.out.println("Initializing socket on port " + port);
                synchronized (Lock_serverSocket) {
                    serverSocket = new ServerSocket(port);
                }
            } catch (IOException e) {
                System.err.println("Failed to create Socket!\n");
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        try {
            //Close port
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Failed to close server socket");
                e.printStackTrace();
            }
            running = false;
            //Wait for threads to end
            System.out.println("Stopping threads");
            for(int i = 0 ; i < n_threads ; i++) {
                threads.get(i).join();
            }
        } catch(UnsupportedOperationException | InterruptedException e){
            System.err.println("Failed to stop threads!");
            e.printStackTrace();
        }
        try{
            System.out.println("Stopping server");
            serverSocket.close();
            System.out.println("Server ended");

        } catch (IOException e) {
            System.err.println("Failed to stop Server!\n");
            e.printStackTrace();
        }

    }

    public String getFileContent(String fileName){
        try {
            InputStream fileStream;
            synchronized (Lock_Assets) {
                fileStream = assetManager.open(fileName);
            }
            //Read File
            int size = fileStream.available();
            System.out.println("Size: " + size);
            byte[] content = new byte[size];
            fileStream.read(content);
            String result = new String(content , StandardCharsets.UTF_8);
            return result;
        } catch (IOException e) {
            System.err.println("Failed to open file");
            e.printStackTrace();
            return "";
        }
    }


    private class GetIP_t extends Thread{
        @Override
        public void run() {
            try {
                final DatagramSocket tmpsocket = new DatagramSocket();
                try {
                    tmpsocket.connect(Inet4Address.getByName("8.8.8.8"), 6969);
                    String ip = tmpsocket.getLocalAddress().getHostAddress();
                    synchronized (lock) {
                        IP = ip;
                    }
                } catch(UnknownHostException e) {
                    e.printStackTrace();
                }
            } catch(SocketException e){
                e.printStackTrace();
            }


        }
    }

    private class setPort extends Thread{
        int port = 8080;

        setPort(int port){
            this.port = port;
        }
         @Override
        public void run(){

         }
    }

    public boolean isRunning(){
        synchronized (Lock_running){
            return running;
        }
    }
}


