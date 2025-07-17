package servlets;
import server.RequestParser.RequestInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;
import graph.Message;

public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        // Implementation for handling HTML loading requests
        // This could involve reading an HTML file and writing it to the output stream
        Map<String, String> params = ri.getParameters();
        String topicStr = params.get("topic");
        String messageStr = params.get("message");
        System.out.println("Topic: " + topicStr);
        System.out.println("Message: " + messageStr);
                
        TopicManager tm=TopicManagerSingleton.get(); // Get the singleton instance of TopicManager
        if (topicStr == null || topicStr.isEmpty()) {
            String error = "<html><body><h1>Topic not specified</h1></body></html>";
            String headers = "HTTP/1.1 400 Bad Request\r\nContent-Type: text/html; charset=UTF-8\r\nContent-Length: " + error.getBytes().length + "\r\n\r\n";
            toClient.write(headers.getBytes());
            toClient.write(error.getBytes());
            toClient.flush();
            return;
        }
        if (messageStr == null || messageStr.isEmpty()) {
            String error = "<html><body><h1>Message not specified</h1></body></html>";
            String headers = "HTTP/1.1 400 Bad Request\r\nContent-Type: text/html; charset=UTF-8\r\nContent-Length: " + error.getBytes().length + "\r\n\r\n";
            toClient.write(headers.getBytes());
            toClient.write(error.getBytes());
            toClient.flush();
            return;
        }

        // Publish the message to the specified topic
        Message msg = new Message(messageStr);
        tm.getTopic(topicStr).publish(msg);
                
        // Add the message to the least values map
        tm.addLeastValue(topicStr, msg); // Add the message to the least values map
        ConcurrentHashMap<String, Message> updateLeastValuesMap = tm.getLeastValuesMap();

        StringBuilder html = new StringBuilder();
        html.append("""
        <html><head>
        <meta charset="UTF-8">
        <title>Topics Info</title>
        <style>
            body{
            font-family:'Montserrat',Arial,sans-serif;
            margin:18px;
            color:#333;
            }
            h2{
            color:#1976d2;
            margin-bottom:14px;
            font-size:1.1rem;
            }
            table{
            width:100%;
            border-collapse:collapse;
            border-radius:10px;
            overflow:hidden;
            box-shadow:0 2px 8px #ddd;
            background:#fff;
            }
            thead{
            background:#e3f2fd;
            }
            th,td{
            padding:10px 14px;
            text-align:left;
            }
            tbody tr:nth-child(even){
            background:#f9f9f9;
            }
            tbody tr:hover{
            background:#eaf4ff;
            }
            .status{
            margin-bottom:18px;
            padding:8px 12px;
            background:#e8f5e9;
            color:#2e7d32;
            border:1px solid #c8e6c9;
            border-radius:6px;
            font-size:0.9rem;
            }
        </style>
        </head><body>
        """);

        // small message that shows that the message was published
        html.append("<div class='status'>Message &quot;")
            .append(escapeHtml(messageStr))
            .append("&quot; published to topic <strong>")
            .append(escapeHtml(topicStr))
            .append("</strong>.</div>");

        html.append("<h2>Topics Info</h2>");
        html.append("<table><thead><tr><th>Topic Name</th><th>Last Value</th></tr></thead><tbody>");

        for (Map.Entry<String, Message> entry : updateLeastValuesMap.entrySet()) {
            String topicName = entry.getKey();
            Message lastMessage = entry.getValue();
            html.append("<tr><td>")
                .append(escapeHtml(topicName))
                .append("</td><td>")
                .append(escapeHtml(lastMessage.asText))
                .append("</td></tr>");
        }
        html.append("</tbody></table></body></html>");

        byte[] content = html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String headers = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: text/html; charset=UTF-8\r\n"
            + "Content-Length: " + content.length + "\r\n"
            + "Connection: close\r\n"
            + "\r\n";
        toClient.write(headers.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        toClient.write(content);
    }
    
    // helper to avoid breaking on angle‑brackets
    private static String escapeHtml(String s){
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    @Override
    public void close() throws IOException {
        // Clean up resources if necessary
    }
    
}