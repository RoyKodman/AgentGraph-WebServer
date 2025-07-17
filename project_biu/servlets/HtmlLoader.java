package servlets;
import server.RequestParser.RequestInfo;
import java.io.IOException;
import java.io.OutputStream;

public class HtmlLoader implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        // Implementation for handling HTML loading requests
        // This could involve reading an HTML file and writing it to the output stream

    }

    @Override
    public void close() throws IOException {
        // Clean up resources if necessary
    }
    
}
