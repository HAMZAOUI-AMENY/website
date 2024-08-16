import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

public class SignupHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            serveSignupPage(exchange, "");
        } else if ("POST".equals(exchange.getRequestMethod())) {
            handleSignup(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    private void serveSignupPage(HttpExchange exchange, String message) throws IOException {
        String htmlResponse = new String(Files.readAllBytes(Paths.get("C:\\Users\\Prepa\\OneDrive\\Bureau\\JavaProject\\web\\signup.html")), StandardCharsets.UTF_8);
        htmlResponse = htmlResponse.replace("<!--MESSAGE_PLACEHOLDER-->", message);
        exchange.sendResponseHeaders(200, htmlResponse.length());
        OutputStream os = exchange.getResponseBody();
        os.write(htmlResponse.getBytes());
        os.close();
    }

    private void handleSignup(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String decodedBody = URLDecoder.decode(requestBody, StandardCharsets.UTF_8);
        String[] keyValuePairs = decodedBody.split("&");

        String matricule = "";
        String nom = "";
        String profil = "";
        String motdepasse = "";

        for (String pair : keyValuePairs) {
            String[] entry = pair.split("=");
            if (entry.length > 1) {
                String key = entry[0];
                String value = entry[1];

                switch (key) {
                    case "matricule":
                        matricule = value;
                        break;
                    case "nom":
                        nom = value;
                        break;
                    case "profil":
                        profil = value;
                        break;
                    case "motdepasse":
                        motdepasse = value;
                        break;
                }
            }
        }

        UserService userService = new UserService();
        String message;
        try {
            boolean userAdded = userService.addUser(Integer.parseInt(matricule), nom, profil, motdepasse);
            if (userAdded) {
                message = "<p style='color: green;'>User added successfully.</p>";
            } else {
                message = "<p style='color: red;'>Failed to add user.</p>";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            message = "<p style='color: red;'>Error: Database error occurred.</p>";
        }

        serveSignupPage(exchange, message);
    }
}
