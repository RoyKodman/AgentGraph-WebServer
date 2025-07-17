package server;

import servlets.Servlet;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainTrain { // RequestParser

    private static void testParseRequest() {
        // Test data
        String request = "GET /api/resource?id=123&name=test HTTP/1.1\n" +
                "Host: example.com\n" +
                "Content-Length: 5\n"+
                "\n" +
                "filename=\"hello_world.txt\"\n"+
                "\n" +
                "hello world!\n"+
                "\n" ;

        BufferedReader input = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.getBytes())));
        try {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(input);

            // Test HTTP command
            if (!requestInfo.getHttpCommand().equals("GET")) {
                System.out.println("HTTP command test failed (-5)");
            }

            // Test URI
            if (!requestInfo.getUri().equals("/api/resource?id=123&name=test")) {
                System.out.println("URI test failed (-5)");
            }

            // Test URI segments
            String[] expectedUriSegments = {"api", "resource"};
            if (!Arrays.equals(requestInfo.getUriSegments(), expectedUriSegments)) {
                System.out.println("URI segments test failed (-5)");
                for(String s : requestInfo.getUriSegments()){
                    System.out.println(s);
                }
            }
            // Test parameters
            Map<String, String> expectedParams = new HashMap<>();
            expectedParams.put("id", "123");
            expectedParams.put("name", "test");
            expectedParams.put("filename","\"hello_world.txt\"");
            if (!requestInfo.getParameters().equals(expectedParams)) {
                System.out.println("Parameters test failed (-5)");
            }

            // Test content
            byte[] expectedContent = "hello world!\n".getBytes();
            if (!Arrays.equals(requestInfo.getContent(), expectedContent)) {
                System.out.println("Content test failed (-5)");
            }
            input.close();
        } catch (IOException e) {
            System.out.println("Exception occurred during parsing: " + e.getMessage() + " (-5)");
        }
    }


    public static void testServer() throws Exception {
        // Record initial thread set
        java.util.Set<Thread> beforeThreads = new java.util.HashSet<>(Thread.getAllStackTraces().keySet());

        // Find a random available port
        java.net.ServerSocket tempSocket = new java.net.ServerSocket(0);
        int port = tempSocket.getLocalPort();
        tempSocket.close();

        // Define the test servlet
        class SumServlet implements Servlet {
            @Override
            public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
                Map<String, String> params = ri.getParameters();
                String aStr = params.get("a");
                String bStr = params.get("b");
                int a, b;
                try {
                    a = Integer.parseInt(aStr);
                    b = Integer.parseInt(bStr);
                } catch (Exception e) {
                    String body = "sum=error";
                    toClient.write(body.getBytes());
                    toClient.flush();
                    return;
                }
                int sum = a + b;
                String body = "\n"+"sum=" + sum + "\n";
                toClient.write(body.getBytes());
                toClient.flush();
            }
            @Override
            public void close() throws IOException {}
        }

        MyHTTPServer server = new MyHTTPServer(port, 1);
        server.addServlet("GET", "/sum", new SumServlet());
        server.start();

        // Wait for server to start
        Thread.sleep(200);

        // Check thread count (should be one new thread)
        java.util.Set<Thread> afterThreads = new java.util.HashSet<>(Thread.getAllStackTraces().keySet());
        afterThreads.removeAll(beforeThreads);
        int newThreads = 0;
        for (Thread t : afterThreads) {
            if (t.isAlive() && t != Thread.currentThread()) newThreads++;
        }
        if (newThreads != 1) {
            System.out.println("Error: Expected 1 new thread, found " + newThreads);
            server.close();
            return;
        }

        // Client: connect and send GET request
        java.net.Socket client = new java.net.Socket("localhost", port);
        java.io.OutputStream out = client.getOutputStream();
        java.io.InputStream in = client.getInputStream();
        String req = "GET /sum?a=5&b=2 HTTP/1.1\n" +
                "Host: localhost\n" +
                "Content-Length: 5\n"+
                "\n" +
                "filename=\"hello_world.txt\"\n"+
                "\n" +
                "hello world!\n"+
                "\n" ;


        out.write(req.getBytes());
        out.flush();

        // Read response
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
        }

        if (!response.toString().contains("sum=7")) {
            System.out.println("Error: Response does not contain sum=7. Got: " + response);
        }

        // Cleanup
        client.close();
        server.close();

        // Wait for threads to finish
        Thread.sleep(2000);
        boolean leak = false;
        for (Thread t : afterThreads) {
            if (t.isAlive() && t != Thread.currentThread()) {
                leak = true;
                break;
            }
        }
        if (leak) {
            System.out.println("Error: Thread leak detected after server shutdown");
            return;
        }
        System.out.println("Test passed");
    }

    public static void main(String[] args) {
        testParseRequest(); // 40 points
        try {
            testServer(); // 60
        } catch(Exception e) {
            System.out.println("your server throwed an exception (-60)");
        }
        System.out.println("done");
    }

}
