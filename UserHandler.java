import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UserHandler implements HttpHandler {
    private static final String HTML_FILE_PATH = "C:\\Users\\Prepa\\OneDrive\\Bureau\\JavaProject\\web\\user.html";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Read the HTML file content
        byte[] response = Files.readAllBytes(Paths.get(HTML_FILE_PATH));
        
        // Set the response headers
        exchange.getResponseHeaders().set("Content-Type", "text/html"); // Set content type to HTML
        exchange.sendResponseHeaders(200, response.length);
        
        // Write response body
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}
