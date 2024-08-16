import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Scanner;

public class UserPage implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "User page content here"; // Replace with actual user page content
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public void display() {
        UserSession session = UserSession.getInstance();
        if (session == null) {
            System.out.println("User session not found. Please log in first.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("Welcome to the User Page!");
            System.out.println("Select an option:");
            System.out.println("1. Add new operation");
            System.out.println("2. Exit");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addOperation();
                    break;
                case "2":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private void addOperation() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter location: ");
        String location = scanner.nextLine();

        System.out.print("Enter operation type (entrée/sortie): ");
        String operationType = scanner.nextLine();

        String barcode = readBarcode();
        if (barcode == null || barcode.isEmpty()) {
            System.out.println("No barcode scanned.");
            return;
        }

        try {
            OperationService operationService = new OperationService();
            operationService.addOperation(location, operationType, barcode);
            System.out.println("Operation added successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to add operation");
        }
    }

    private String readBarcode() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please scan a barcode:");

        if (scanner.hasNextLine()) {
            String barcode = scanner.nextLine();
            if (!barcode.isEmpty()) {
                System.out.println("Barcode scanned: " + barcode);
                return barcode;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        UserSession session = UserSession.getInstance(465778); // Example matricule for testing
        UserPage userPage = new UserPage();
        userPage.display();
    }
}
