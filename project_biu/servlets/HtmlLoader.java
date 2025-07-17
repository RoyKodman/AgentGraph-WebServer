package servlets;

import server.RequestParser.RequestInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HtmlLoader implements Servlet {
    private String directory;

    public HtmlLoader(String directory) {
        this.directory = directory;
    }

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        // Extract requested HTML file name
        String fileName = ri.getUriSegments()[1];
        
        System.out.println("Requested file: " + fileName);
        // Security check: prevent directory traversal
        if (fileName == null || fileName.contains("..")) {
            String error = "<html><body><h1>Invalid file request</h1></body></html>";
            toClient.write(error.getBytes());
            return;
        }

        // Build the file path from the directory and file name
        String filePath = this.directory + '/' + fileName;
        System.out.println(filePath);
        try {
            // Load file content and send response
            System.out.println("Loading file: " + filePath);
            byte[] content = Files.readAllBytes(Paths.get(filePath));
            // Prepare HTTP response headers
            String headers = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Length: " + content.length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";
            toClient.write(headers.getBytes(StandardCharsets.UTF_8));
            toClient.write(content);
            toClient.flush();
            return;
        } catch (IOException e) {
            // Handle file not found or read error
            String error = "<html><body><h1>File not found: " + fileName + "</h1></body></html>";
            String headers = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\nContent-Length: " + error.length() + "\r\n\r\n";
            toClient.write(headers.getBytes());
            toClient.write(error.getBytes());
            toClient.flush();
            return;
        }
    }

    @Override
    public void close() throws IOException {
        // No resources to close in this simple loader
    }
}