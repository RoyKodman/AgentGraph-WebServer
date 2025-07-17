package server;

import java.io.*;
import java.net.*;

public class SimpleClient {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 5000;

        // Connect to the server
        Socket socket = new Socket(host, port);

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send a message to the server
        out.println("Hi Server!");

        String response = in.readLine();
        System.out.println("Server replied: " + response);

        // Close the connection
        socket.close();
    }
}
