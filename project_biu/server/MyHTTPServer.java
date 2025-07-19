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

import server.RequestParser.RequestInfo;

import static server.RequestParser.parseRequest;

/**
 * MyHTTPServer is a simple, reusable multi-threaded HTTP server implementation.
 * <p>
 * It allows developers to register servlets for specific HTTP methods and URI prefixes.
 * Each incoming request is dispatched to the appropriate servlet in a thread pool.
 * </p>
 * <h2>Usage Example</h2>
 * <pre>{@code
 * HTTPServer server = new MyHTTPServer(8080, 5);
 * server.addServlet("GET", "/hello", new MyHelloServlet());
 * server.start();
 * // ...
 * server.close();
 * }</pre>
 * <p>
 * The server runs in its own thread. Use {@link #addServlet(String, String, Servlet)} to register handlers.
 * </p>
 *
 * @see HTTPServer
 * @see servlets.Servlet
 */
public class MyHTTPServer extends Thread implements HTTPServer{
    // Maps for HTTP method to URI to Servlet
    Map<String, Servlet> getMap, postMap, deleteMap;
    // Server running flag
    boolean run;
    // Thread pool for handling client requests
    ExecutorService pool;
    // Main server socket
    ServerSocket serverSocket;

    /**
     * Constructs a new HTTP server listening on the given port, using a fixed thread pool.
     *
     * @param port     The TCP port to listen on (e.g., 8080)
     * @param nThreads Number of worker threads for handling requests
     * @throws IOException if the server socket cannot be created
     */
    public MyHTTPServer(int port,int nThreads) throws IOException {
        // Initialize servlet maps and thread pool
        getMap = new ConcurrentHashMap<>();
        postMap = new ConcurrentHashMap<>();
        deleteMap = new ConcurrentHashMap<>();
        run = false;
        pool = Executors.newFixedThreadPool(nThreads);
        serverSocket = new ServerSocket(port);
    }

    /**
     * Register a servlet to handle a specific HTTP method and URI prefix.
     *
     * @param httpCommanmd HTTP method (e.g., "GET", "POST", "DELETE")
     * @param uri URI prefix to match (e.g., "/api/")
     * @param s   The servlet to handle matching requests
     */
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

    /**
     * Remove a servlet for a specific HTTP method and URI prefix.
     *
     * @param httpCommanmd HTTP method (e.g., "GET", "POST", "DELETE")
     * @param uri URI prefix to remove
     */
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

    /**
     * Main server loop: accepts and handles client connections.
     * <p>
     * This method is called when the server thread is started. It should not be called directly.
     * </p>
     */
    @Override
    public void run() {
        run = true;
        try {
            serverSocket.setSoTimeout(1000);
            while (run) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    pool.submit(() -> handleClient(clientSocket));
                } catch (SocketTimeoutException e) {
                    // timeout every second to check `running`
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private void handleClient(Socket clientSocket) {
        System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());

        try (
            InputStream in        = clientSocket.getInputStream();
            OutputStream toClient = clientSocket.getOutputStream();
        ) {
            System.out.println("Handling client request...");
            // Only use InputStream for parsing!
            RequestInfo info = RequestParser.parseRequest(in);
            if (info == null) {
                clientSocket.close();
                return;
            }

            Servlet servlet = null;
            switch (info.getHttpCommand()) {
                case "GET":    servlet = getLongestMatchingServlet(getMap, info.getUri());    break;
                case "POST":   servlet = getLongestMatchingServlet(postMap, info.getUri());   System.out.println(servlet); break;
                case "DELETE": servlet = getLongestMatchingServlet(deleteMap, info.getUri()); break;
            }

            if (servlet != null) {
                servlet.handle(info, toClient);
            } else {
                toClient.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
            toClient.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    // Main server loop: accept and handle client connections
    /*public void run(){
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
    } */

    /**
     * Signal the server to stop and release all resources.
     * <p>
     * After calling this method, the server will stop accepting new connections and shut down the thread pool.
     * </p>
     */
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
