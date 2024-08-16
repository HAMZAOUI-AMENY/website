import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchHandler implements HttpHandler {
    private static final Logger logger = Logger.getLogger(SearchHandler.class.getName());
    private OperationService operationService = new OperationService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String barcode = exchange.getRequestURI().getQuery().split("=")[1];
            try {
                String response = operationService.searchOperation(barcode);
                sendJsonResponse(exchange, response, 200);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL error while searching operations", e);
                sendJsonResponse(exchange, "{\"status\":\"error\"}", 500);
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    private void sendJsonResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
