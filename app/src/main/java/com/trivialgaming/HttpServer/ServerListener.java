package com.trivialgaming.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListener extends Thread {
    private ServerSocket serverSocket;
    private Server server;
    private static int newID;
    private int ID;
    private static final Object Log_Lock = new Object();

    ServerListener(Server server, ServerSocket serverSocket) {
        newID++;
        this.server = server;
        this.serverSocket = serverSocket;
        this.ID = newID;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        System.out.println(ID + ") Starting listening thread");
        if (server.getServerSocket().isClosed()) {
            System.out.println(ID + ") Socket is closed");
            return;
        }

        try {
            while (true) {
                // Wait for connection
                Socket socket = serverSocket.accept();
                synchronized (Log_Lock) {
                    System.out.println(ID + ") Received a connection!");
                }

                try {
                    inputStream = socket.getInputStream();

                    // Read the request from the client
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder requestBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {
                        requestBuilder.append(line).append("\r\n");
                    }
                    String strReq = requestBuilder.toString();
                    System.out.println(ID + ") buffer: " + strReq);
                    HttpParser.HttpRequest request = new HttpParser.HttpRequest(strReq);
                    if (!request.raw.isEmpty()) System.out.println(ID + ") Received request: " + request.raw);

                    // Handle request
                    String response = "";
                    System.out.println(ID + ") Method: " + request.method);
                    switch (request.method) {
                        case "GET":
                            // GET Request
                            //check if file argument was passed
                            if (request.file == "") {
                                // No file argument or index
                                System.err.println(ID + ") Improper GET request");
                                response = HttpParser.CreateResponse_403(server.getFileContent("NotFound.html"), "text/html");
                            } else {
                                // Other files
                                //Check if default page
                                if(request.file.equals("/")) request.file = "index.html";
                                String content = server.getFileContent(request.file);
                                if (content == "") {
                                    // Requested file does not exist
                                    System.err.println(ID + ") Requested file " + request.file + " does not exist");
                                    response = HttpParser.CreateResponse_403(server.getFileContent("NotFound.html"), "text/html");
                                } else {
                                    // File exists
                                    response = HttpParser.CreateResponse(content, "text/html");
                                }
                            }
                    }

                    // Response
                    synchronized (Log_Lock) {
                        System.out.println(ID + ") Writing response: " + response);
                    }
                    outputStream = socket.getOutputStream();
                    outputStream.write(response.getBytes());
                } catch (IOException e) {
                    System.err.println(ID + ") Failed to read from the socket!");
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            System.err.println(ID + ") Failed to close the input stream!");
                            e.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            System.err.println(ID + ") Failed to close the output stream!");
                            e.printStackTrace();
                        }
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            System.err.println(ID + ") Failed to close the socket!");
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(ID + ") Failed to get connection");
            e.printStackTrace();
        } finally {
            System.out.println(ID + ") Ending thread");
        }
    }
}
