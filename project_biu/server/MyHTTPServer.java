package server;
import servlets.Servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static server.RequestParser.parseRequest;

public class MyHTTPServer extends Thread implements HTTPServer{
    // Maps for HTTP method to URI to Servlet
    Map<String, Servlet> getMap, postMap, deleteMap;
    // Server running flag
    boolean run;
    // Thread pool for handling client requests
    ExecutorService pool;
    // Main server socket
    ServerSocket serverSocket;

    public MyHTTPServer(int port,int nThreads) throws IOException {
        // Initialize servlet maps and thread pool
        getMap = new ConcurrentHashMap<>();
        postMap = new ConcurrentHashMap<>();
        deleteMap = new ConcurrentHashMap<>();
        run = false;
        pool = Executors.newFixedThreadPool(nThreads);
        serverSocket = new ServerSocket(port);
    }

    // Register a servlet for a specific HTTP method and URI
    public void addServlet(String httpCommanmd, String uri, Servlet s){
        switch (httpCommanmd) {
            case "GET":
                getMap.put(uri, s);
                break;
            case "POST":
                postMap.put(uri, s);
                break;
            case "DELETE":
                deleteMap.put(uri, s);
                break;
            default:
                break;
        }
    }

    // Remove a servlet for a specific HTTP method and URI
    public void removeServlet(String httpCommanmd, String uri){
        switch (httpCommanmd) {
            case "GET":
                getMap.remove(uri);
                break;
            case "POST":
                postMap.remove(uri);
                break;
            case "DELETE":
                deleteMap.remove(uri);
                break;
            default:
                break;
        }
    }

    // Main server loop: accept and handle client connections
    public void run(){
        this.run = true;
        try {
            // Set timeout for accept to allow periodic shutdown check
            serverSocket.setSoTimeout(1000);
            while (this.run) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    // Handle each client in a thread pool task
                    pool.submit(() -> {
                        System.out.println("New client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                        try (
                                InputStream in = clientSocket.getInputStream();
                                BufferedReader fromClient = new BufferedReader(new InputStreamReader(in));
                                OutputStream toClient = clientSocket.getOutputStream();
                        ) {
                            System.out.println("Ori");
                            RequestParser.RequestInfo info = parseRequest(fromClient, in);
                            System.out.println("Request info: " + info);
                            if (info == null) {
                                clientSocket.close();
                                return;
                            }
                            System.out.println("Request info: " + info);
                            Servlet s = null;
                            switch (info.getHttpCommand()) {
                                case "GET":    s = getLongestMatchingServlet(getMap, info.getUri());    break;
                                case "POST":   s = getLongestMatchingServlet(postMap, info.getUri());   break;
                                case "DELETE": s = getLongestMatchingServlet(deleteMap, info.getUri()); break;
                                default: {
                                    return;
                                }
                            }
                            // Handle the request or return 404
                            if (s != null) {
                                s.handle(info, toClient);
                                toClient.flush();
                            } else {
                                String errorResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
                                toClient.write(errorResponse.getBytes());
                                toClient.flush();
                            }
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (SocketTimeoutException e) {}
            }
        } catch (IOException e) {}
        // Shutdown: close thread pool and server socket
        finally {
            try {
                pool.shutdown();
                serverSocket.close();
            } catch (IOException e) {}
        }
    }

    // Signal the server to stop
    public void close(){
        this.run = false;
        /*
        // Optionally close all servlets
        for (Servlet s : getMap.values()){
            s.close();
        }
        for (Servlet s : postMap.values()){
            s.close();
        }
        for (Servlet s : deleteMap.values()){
            s.close();
        }
        */
    }

    // Find the servlet with the longest matching URI prefix
    private static Servlet getLongestMatchingServlet(Map<String, Servlet> map, String path) {
        Servlet result = null;
        int longest = -1;
        for (String key : map.keySet()) {
            if (path.startsWith(key) && key.length() > longest) {
                longest = key.length();
                result = map.get(key);
            }
        }
        return result;
    }
}
