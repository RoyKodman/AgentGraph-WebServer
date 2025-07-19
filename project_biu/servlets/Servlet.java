package servlets;

import java.io.IOException;
import java.io.OutputStream;

import server.RequestParser.RequestInfo;

/**
 * Servlet defines the contract for handling HTTP requests in the custom HTTP server.
 * <p>
 * Developers should implement this interface to create custom request handlers (servlets).
 * Each servlet can be registered to handle specific HTTP methods and URI patterns.
 * </p>
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class MyHelloServlet implements Servlet {
 *     public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
 *         String response = "<html><body>Hello, world!</body></html>";
 *         toClient.write(response.getBytes(StandardCharsets.UTF_8));
 *     }
 *     public void close() {}
 * }
 * }
 * </pre>
 *
 * @see server.HTTPServer
 * @see server.MyHTTPServer
 * @see server.RequestParser.RequestInfo
 */
public interface Servlet {
    /**
     * Handle an HTTP request and write a response to the client.
     *
     * @param ri       Parsed request information (method, URI, headers, body, etc.)
     * @param toClient Output stream to write the HTTP response
     * @throws IOException if an I/O error occurs
     */
    void handle(RequestInfo ri, OutputStream toClient) throws IOException;

    /**
     * Release any resources held by this servlet (optional).
     *
     * @throws IOException if an I/O error occurs
     */
    void close() throws IOException;
}
