import server.HTTPServer;
import server.MyHTTPServer;
import servlets.ConfLoader;
import servlets.HtmlLoader;
import servlets.TopicDisplayer;


public class Main {
    public static void main(String[] args) throws Exception{
        // Create the HTTP server
        HTTPServer server=new MyHTTPServer(8080,5);
        // Register servlets
        server.addServlet("GET", "/publish", new TopicDisplayer());
        // server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
        // Start and stop the server
        server.start();
        System.out.println("Server started on port 8080");
        System.in.read();
        server.close();
        System.out.println("done");
    }
}
