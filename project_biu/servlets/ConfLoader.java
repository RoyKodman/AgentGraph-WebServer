package servlets;

import server.RequestParser.RequestInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import configs.GenericConfig;
import graph.Graph;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;
import views.HtmlGraphWriter;

/**
 * ConfLoader is a servlet that handles HTTP POST requests for uploading configuration files.
 * <p>
 * It processes multipart/form-data uploads, saves the configuration file, updates the topic manager,
 * creates a new computation graph, and responds with an HTML visualization of the graph.
 * </p>
 * <h2>Usage</h2>
 * Register this servlet for POST requests to a specific URI:
 * <pre>{@code
 * server.addServlet("POST", "/upload", new ConfLoader());
 * }</pre>
 */
public class ConfLoader implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        String contentType = ri.getHeader("Content-Type");
        byte[] body = ri.getContent();
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            try {
                UploadFile upload = parseMultipartFile(body, contentType);
                Path dir = Paths.get("config_files");
                if (Files.notExists(dir)) {
                    Files.createDirectories(dir);
                }
                Path target = dir.resolve(upload.filename);
                Files.write(target, upload.content);
                System.out.println("File uploaded: " + upload.filename);

                TopicManager tm = TopicManagerSingleton.get();
                tm.clear();

                GenericConfig gc = new GenericConfig();
                gc.setConfFile("config_files/" + upload.filename);
                gc.create();  
                System.out.println("Configuration created ");
                
                Graph g = new Graph();
                g.createFromTopics();
                System.out.println("Graph nodes: " + g.size());
                System.out.println("Graph created from topics");
                
                // Send HTTP headers first
                String headers = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html; charset=UTF-8\r\n"
                    + "Connection: close\r\n\r\n";
                toClient.write(headers.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                
                // Use HtmlGraphWriter to write the graph HTML directly to the client
                OutputStreamWriter writer = new OutputStreamWriter(toClient, java.nio.charset.StandardCharsets.UTF_8);
                HtmlGraphWriter.write(g, writer);
                writer.flush();

            } catch (Exception e) {
                e.printStackTrace();
                // Send error response
                String errorHtml = "<html><body><h2>Error uploading file</h2><p>" + e.getMessage() + "</p></body></html>";
                byte[] content = errorHtml.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                String headers = "HTTP/1.1 500 Internal Server Error\r\n"
                    + "Content-Type: text/html; charset=UTF-8\r\n"
                    + "Content-Length: " + content.length + "\r\n"
                    + "Connection: close\r\n\r\n";
                toClient.write(headers.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                toClient.write(content);
            }
        }
    }

    @Override
    public void close() throws IOException {}

    public static class UploadFile {
        public String filename;
        public byte[] content;
    }

    private static UploadFile parseMultipartFile(byte[] body, String contentType) throws Exception {
        int idx = contentType.indexOf("boundary=");
        if (idx < 0) throw new Exception("Boundary not found");
        String boundary = contentType.substring(idx + "boundary=".length());
        if (!boundary.startsWith("--")) boundary = "--" + boundary;
        boundary = "--" + boundary;
        System.out.println("DEBUG: Boundary: [" + boundary + "]");
        String bodyStr = new String(body, java.nio.charset.StandardCharsets.ISO_8859_1);
        System.out.println("DEBUG: First 400 of bodyStr: [" + bodyStr.substring(0, Math.min(400, bodyStr.length())) + "]");

        int partStart = bodyStr.indexOf(boundary);
        if (partStart < 0) throw new Exception("Boundary not found in body");
        int dispIdx = bodyStr.indexOf("Content-Disposition", partStart);
        int filenameIdx = bodyStr.indexOf("filename=\"", dispIdx);
        int filenameEnd = bodyStr.indexOf("\"", filenameIdx + 10);
        String filename = bodyStr.substring(filenameIdx + 10, filenameEnd);

        int doubleNewline = bodyStr.indexOf("\r\n\r\n", filenameEnd);
        int contentStart = doubleNewline + 4;
        int partEnd = bodyStr.indexOf(boundary, contentStart) - 2;

        byte[] fileContent = java.util.Arrays.copyOfRange(body, contentStart, partEnd);

        UploadFile upload = new UploadFile();
        upload.filename = filename;
        upload.content = fileContent;
        return upload;
    }
}