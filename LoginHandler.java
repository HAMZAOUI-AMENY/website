import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginHandler implements HttpHandler {

    private static final Logger logger = Logger.getLogger(LoginHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            handleLogin(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String[] credentials = requestBody.split("&");
        int matricule = Integer.parseInt(credentials[0].split("=")[1]);
        String motdepasse = credentials[1].split("=")[1];

        UserService userService = new UserService();
        try {
            if (userService.authenticateUser(matricule, motdepasse)) {
                String userProfile = userService.getUserProfile(matricule).toLowerCase();

                // Store the matricule in a cookie
                HttpCookie cookie = new HttpCookie("user_matricule", String.valueOf(matricule));
                cookie.setMaxAge(3600); // Set cookie to expire in 1 hour
                exchange.getResponseHeaders().add("Set-Cookie", cookie.toString());

                String jsonResponse = "{\"status\":\"success\", \"profile\":\"" + userProfile + "\"}";
                sendJsonResponse(exchange, jsonResponse, 200); // OK status
            } else {
                sendJsonResponse(exchange, "{\"status\":\"fail\"}", 401); // Unauthorized
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL error during login", e);
            sendJsonResponse(exchange, "{\"status\":\"error\"}", 500); // Internal Server Error
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during login", e);
            sendJsonResponse(exchange, "{\"status\":\"error\"}", 500); // Internal Server Error
        }
    }

    private void sendJsonResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
