package server;

import servlets.Servlet;

/**
 * HTTPServer defines the API for a simple, reusable HTTP server.
 * <p>
 * Developers can implement this interface to create a custom HTTP server, or use the provided {@link MyHTTPServer} implementation.
 * Servlets can be registered to handle specific HTTP methods and URI patterns.
 * </p>
 * <h2>Usage Example</h2>
 * <pre>{@code
 * HTTPServer server = new MyHTTPServer(8080, 5);
 * server.addServlet("GET", "/hello", new MyHelloServlet());
 * server.start();
 * // ...
 * server.close();
 * }</pre>
 *
 * @see MyHTTPServer
 * @see servlets.Servlet
 */
public interface HTTPServer extends Runnable {
    /**
     * Register a servlet to handle a specific HTTP method and URI prefix.
     *
     * @param httpCommanmd HTTP method (e.g., "GET", "POST", "DELETE")
     * @param uri URI prefix to match (e.g., "/api/")
     * @param s   The servlet to handle matching requests
     */
    void addServlet(String httpCommanmd, String uri, Servlet s);
    void removeServlet(String httpCommanmd, String uri);
    void start();
    void close();
}
