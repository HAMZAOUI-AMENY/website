import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.Optional;

public class AddOperationHandler implements HttpHandler {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/evenements";
    private static final String USER = "root";
    private static final String PASSWORD = "Mysql24";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        if ("POST".equals(exchange.getRequestMethod())) {
            // Decode URL-encoded data
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String decodedBody = URLDecoder.decode(requestBody, StandardCharsets.UTF_8);
            String[] keyValuePairs = decodedBody.split("&");

            String location = "";
            String operationType = "";
            String barcode = "";

            for (String pair : keyValuePairs) {
                String[] entry = pair.split("=");
                if (entry.length > 1) {
                    String key = entry[0];
                    String value = entry[1];

                    switch (key) {
                        case "location":
                            location = value;
                            break;
                        case "operationType":
                            operationType = value;
                            break;
                        case "barcode":
                            barcode = value;
                            break;
                    }
                }
            }

            try {
                int userId = getCurrentUserId(exchange); // Retrieve matricule from the cookie
                if (userId != -1) {
                    response = addOperation(userId, location, operationType, barcode);
                } else {
                    response = "Error: User not authenticated.";
                    exchange.sendResponseHeaders(401, response.getBytes().length); // Unauthorized
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response = "Error: Database error occurred.";
                exchange.sendResponseHeaders(500, response.getBytes().length); // Internal Server Error
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, response.getBytes().length); // OK
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    private String addOperation(int userId, String location, String operationType, String barcode) throws SQLException {
        String lastOperationQuery = "SELECT typeOperation, location FROM operations WHERE code_barres = ? ORDER BY dateOperation DESC LIMIT 1";
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement checkStatement = connection.prepareStatement(lastOperationQuery)) {
            checkStatement.setString(1, barcode);
            try (ResultSet resultSet = checkStatement.executeQuery()) {
                if (resultSet.next()) {
                    String lastOperationType = resultSet.getString("typeOperation");
                    String lastLocation = resultSet.getString("location");

                    if (lastOperationType.equals(operationType) && lastLocation.equals(location)) {
                        return "Error: The same operation type already exists for this barcode in this location.";
                    }
                }
            }
        }

        String insertQuery = "INSERT INTO operations (User_ID, dateOperation, typeOperation, code_barres, location) VALUES (?, NOW(), ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setInt(1, userId);
            insertStatement.setString(2, operationType);
            insertStatement.setString(3, barcode);
            insertStatement.setString(4, location);

            int rowsAffected = insertStatement.executeUpdate();
            if (rowsAffected > 0) {
                return "Operation added successfully.";
            } else {
                return "Failed to add operation.";
            }
        }
    }

    private int getCurrentUserId(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                for (HttpCookie httpCookie : HttpCookie.parse(cookie)) {
                    if ("user_matricule".equals(httpCookie.getName())) {
                        return Integer.parseInt(httpCookie.getValue());
                    }
                }
            }
        }
        return -1; // User not authenticated
    }
}