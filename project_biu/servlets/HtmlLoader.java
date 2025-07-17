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
        // Get the requested HTML file name from parameters (e.g., "file" or "page")
        String fileName = ri.getUriSegments()[1];;
        
        System.out.println("Requested file: " + fileName);
        if (fileName == null || fileName.contains("..")) {
            // Prevent directory traversal and handle missing file parameter
            String error = "<html><body><h1>Invalid file request</h1></body></html>";
            toClient.write(error.getBytes());
            return;
        }

        // Assume HTML files are in a "static" directory at project root
        String filePath = this.directory + '/' + fileName;
        System.out.println(filePath);
        try {
            System.out.println("Loading file: " + filePath);
            byte[] content = Files.readAllBytes(Paths.get(filePath));
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