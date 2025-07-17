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
    Map<String, Servlet> getMap;
    Map<String, Servlet> postMap;
    Map<String, Servlet> deleteMap;
    boolean run;
    ExecutorService pool;
    ServerSocket serverSocket;

    public MyHTTPServer(int port,int nThreads) throws IOException {
        getMap = new ConcurrentHashMap<>();
        postMap = new ConcurrentHashMap<>();
        deleteMap = new ConcurrentHashMap<>();
        run = false;
        pool = Executors.newFixedThreadPool(nThreads);
        serverSocket = new ServerSocket(port);

    }

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

    public void run(){
        this.run = true;
        try {
            // As long as the server is not instructed to shut down,
            // it will wait (again) for the client for one second.
            serverSocket.setSoTimeout(1000);
            while (this.run) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    // If got to here it means that new client joined.
                    // A client has connected and therefore its handling
                    // will be performed as a task of the pool thread.

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

        // When the while end it meas the server ended so we need to close the sockets:
        finally {
            try {
                pool.shutdown();
                serverSocket.close();
            } catch (IOException e) {}
        }
    }

    public void close(){
        this.run = false;
        /*
        for (Servlet s : getMap.values()){
            s.close();
        }""""""
        for (Servlet s : postMap.values()){
            s.close();
        }
        for (Servlet s : deleteMap.values()){
            s.close();
        }
        */

    }


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
