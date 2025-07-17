package servlets;
import server.RequestParser.RequestInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;
import graph.Message;
import graph.Topic;

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
        html.append("<html><head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>Topics Info</title>");
        html.append("<style>");
        html.append("body { font-family: 'Montserrat', Arial, sans-serif; margin: 18px; }");
        html.append(".row { display: flex; align-items: center; margin-bottom: 22px; }");
        html.append(".row label { margin-right: 8px; }");
        html.append(".row input[type=\"file\"] { margin-right: 10px; }");
        html.append(".row button { margin-left: auto; }");
        html.append("form { margin-bottom: 24px; }");
        html.append("label { display: block; margin-bottom: 2px; }");
        html.append("input[type=\"text\"] { width: 150px; padding: 3px; margin-bottom: 6px; display: block; }");
        html.append("</style>");
        html.append("</head><body>");
        html.append("<h2>Topics Info</h2>");
        html.append("<table border='1' style='width: 50%; border-radius:10px; box-shadow: 0 2px 8px #eee; background: #fff;'>");
        html.append("<tr style='background:#e3f2fd;'><th>Topic Name</th><th>Last Value</th></tr>");

        for (Map.Entry<String, Message> entry : updateLeastValuesMap.entrySet()) {
            String topicName = entry.getKey();
            Message lastMessage = entry.getValue();
            html.append("<tr>");
            html.append("<td>").append(topicName).append("</td>");
            html.append("<td>").append(lastMessage.asText).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        html.append("</body></html>");

        byte[] content = html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String headers = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: text/html; charset=UTF-8\r\n"
            + "Content-Length: " + content.length + "\r\n"
            + "Connection: close\r\n"
            + "\r\n";
        toClient.write(headers.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        toClient.write(content);
        

    }

    @Override
    public void close() throws IOException {
        // Clean up resources if necessary
    }
    
}