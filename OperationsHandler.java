import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class OperationsHandler implements HttpHandler {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/evenements";
    private static final String USER = "root";
    private static final String PASSWORD = "Mysql24";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        try {
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

                response = addOperation(location, operationType, barcode);
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else if ("GET".equals(exchange.getRequestMethod())) {
                URI requestURI = exchange.getRequestURI();
                String query = requestURI.getQuery();

                if (query != null && query.startsWith("barcode=")) {
                    // Search by barcode
                    String barcode = URLDecoder.decode(query.substring("barcode=".length()), StandardCharsets.UTF_8);
                    response = searchByBarcode(barcode);
                } else {
                    // Get all operations
                    response = getAllOperations();
                }

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response = "{\"error\":\"Error retrieving operations.\"}";
            exchange.sendResponseHeaders(500, response.getBytes().length); // Internal Server Error
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private String addOperation(String location, String operationType, String barcode) throws SQLException {
        String lastOperationQuery = "SELECT typeOperation, location FROM operations WHERE code_barres = ? ORDER BY dateOperation DESC LIMIT 1";
        Connection connection = null;
        PreparedStatement checkStatement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            checkStatement = connection.prepareStatement(lastOperationQuery);
            checkStatement.setString(1, barcode);
            resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                String lastOperationType = resultSet.getString("typeOperation");
                String lastLocation = resultSet.getString("location");

                // Detailed logging
                System.out.println("Last operation type: " + lastOperationType);
                System.out.println("Last location: " + lastLocation);
                System.out.println("New operation type: " + operationType);
                System.out.println("New location: " + location);

                // Check if the last operation type and location match the new ones
                if (lastOperationType.equals(operationType) && lastLocation.equals(location)) {
                    System.out.println("Duplicate operation detected.");
                    return "Error: The same operation type already exists for this barcode in this location.";
                }
            }

            // Proceed with adding the new operation if the check passes
            String insertQuery = "INSERT INTO operations (User_ID, dateOperation, typeOperation, code_barres, location) VALUES (?, NOW(), ?, ?, ?)";
            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                // Assuming User_ID is retrieved from the session or request context
                int userId = getCurrentUserId(); // Implement this method to get the current user ID
                insertStatement.setInt(1, userId);
                insertStatement.setString(2, operationType);
                insertStatement.setString(3, barcode);
                insertStatement.setString(4, location);

                int rowsAffected = insertStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Operation added successfully.");
                    return "Operation added successfully.";
                } else {
                    System.out.println("Failed to add operation.");
                    return "Failed to add operation.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Database error occurred.";
        } finally {
            // Ensure proper resource management
            if (resultSet != null) resultSet.close();
            if (checkStatement != null) checkStatement.close();
            if (connection != null) connection.close();
        }
    }

    private int getCurrentUserId() {
        // Mock implementation to return a user ID
        // Replace this with actual logic to retrieve the current user ID
        return 12345;
    }

    private String getAllOperations() throws SQLException {
        StringBuilder operationsArray = new StringBuilder();
        operationsArray.append("[");

        String query = "SELECT * FROM operations";

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            boolean first = true;
            while (resultSet.next()) {
                if (!first) {
                    operationsArray.append(",");
                }
                operationsArray.append("{")
                        .append("\"userId\":").append(resultSet.getInt("User_ID")).append(",")
                        .append("\"date\":\"").append(resultSet.getString("dateOperation")).append("\",")
                        .append("\"operationType\":\"").append(resultSet.getString("typeOperation")).append("\",")
                        .append("\"barcode\":\"").append(resultSet.getString("code_barres")).append("\",")
                        .append("\"location\":\"").append(resultSet.getString("location")).append("\"")
                        .append("}");
                first = false;
            }
        }
        operationsArray.append("]");
        return operationsArray.toString();
    }

    private String searchByBarcode(String barcode) throws SQLException {
        JSONObject responseJson = new JSONObject();
        JSONArray operationsArray = new JSONArray();

        String query = "SELECT * FROM operations WHERE code_barres = ? ORDER BY dateOperation DESC";

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, barcode);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean found = false;
                while (resultSet.next()) {
                    if (!found) {
                        String operationType = resultSet.getString("typeOperation");
                        String location = resultSet.getString("location");
                        responseJson.put("status", "success");
                        responseJson.put("lastOperationType", operationType);
                        responseJson.put("lastLocation", location);
                        found = true;
                    }
                    JSONObject operation = new JSONObject();
                    operation.put("userId", resultSet.getInt("User_ID"));
                    operation.put("date", resultSet.getString("dateOperation"));
                    operation.put("operationType", resultSet.getString("typeOperation"));
                    operation.put("barcode", resultSet.getString("code_barres"));
                    operation.put("location", resultSet.getString("location"));
                    operationsArray.put(operation);
                }

                if (!found) {
                    responseJson.put("status", "not found");
                }
                responseJson.put("operations", operationsArray);
            }
        }

        return responseJson.toString();
    }
}
