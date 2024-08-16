import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8123), 0);
        server.createContext("/", new StaticFileHandler("C:\\Users\\Prepa\\OneDrive\\Bureau\\JavaProject\\web"));
        server.createContext("/login", new LoginHandler());
        server.createContext("/logout", new LogoutHandler());
        server.createContext("/signup", new SignupHandler()); // Sign-up page
                

        
        server.createContext("/operations", new OperationsHandler());
        server.createContext("/search", new SearchHandler());
        server.createContext("/add-operation", new AddOperationHandler());
        server.createContext("/admin", new AdminHandler()); // Admin handler
        server.createContext("/user", new UserHandler()); // User handler
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8160"); // Updated port number
    }
}